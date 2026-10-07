# Especificação do backend — previsão de chuva em Belém

## Objetivo

Implementar uma pipeline reproduzível de previsão de chuva para Belém (PA), usando o histórico horário de `dados_brutos.json`. A previsão deve ser produzida por um modelo treinado e validado temporalmente, e exposta ao restante do sistema por uma interface estável do backend.

Este documento descreve o que o backend deve adicionar ou alterar. Ele não prescreve uma arquitetura de aplicação que não esteja presente no repositório. Antes da implementação, inspecione o projeto e encaixe os elementos nos módulos, convenções, persistência e API já existentes.

## Fonte e características dos dados

O arquivo fornecido contém resposta horária do modelo `gfs_global` para as coordenadas solicitadas de Belém:

- Coordenadas solicitadas: latitude `-1.4558`, longitude `-48.5044`.
- Coordenadas retornadas pelo provedor: latitude `-1.4643631`, longitude `-48.515625`.
- Período: `2022-01-01T00:00` a `2025-12-31T23:00`.
- Frequência: horária; 35.064 timestamps, correspondentes às 8.760 horas de cada ano, inclusive as 8.784 horas de 2024 (ano bissexto).
- Fuso informado: UTC/GMT, deslocamento zero. Converter para o fuso local `America/Belem` somente na camada de apresentação/consulta; armazenar e alinhar os instantes com fuso de forma inequívoca.
- Unidade de precipitação: milímetros por hora/intervalo conforme definição da fonte; não somar nem rotular como acumulado diário sem agregação explícita.
- O objeto `response.hourly` possui arrays paralelos: `time` e 22 variáveis numéricas. Validar que todos tenham o mesmo comprimento antes de montar as linhas.

Variáveis disponíveis e unidades declaradas no próprio JSON:

| Campo | Unidade | Uso possível |
|---|---:|---|
| `temperature_2m` | °C | Temperatura próxima à superfície |
| `relative_humidity_2m` | % | Umidade próxima à superfície |
| `dew_point_2m` | °C | Ponto de orvalho |
| `pressure_msl` | hPa | Pressão ao nível do mar |
| `surface_pressure` | hPa | Pressão de superfície |
| `cloud_cover` | % | Cobertura total de nuvens |
| `cloud_cover_low` | % | Nuvens baixas |
| `cloud_cover_mid` | % | Nuvens médias |
| `cloud_cover_high` | % | Nuvens altas |
| `wind_speed_10m` | km/h | Velocidade do vento |
| `wind_direction_10m` | ° | Direção do vento; variável circular |
| `wind_gusts_10m` | km/h | Rajadas |
| `cape` | J/kg | Energia convectiva disponível |
| `total_column_integrated_water_vapour` | kg/m² | Água precipitável integrada |
| `relative_humidity_850hPa` | % | Umidade em 850 hPa |
| `relative_humidity_700hPa` | % | Umidade em 700 hPa |
| `relative_humidity_500hPa` | % | Umidade em 500 hPa |
| `temperature_850hPa` | °C | Temperatura em 850 hPa |
| `temperature_700hPa` | °C | Temperatura em 700 hPa |
| `geopotential_height_500hPa` | m | Altura geopotencial em 500 hPa |
| `rain` | mm | Componente de chuva retornada pela fonte |
| `precipitation` | mm | Precipitação retornada pela fonte; alvo inicial recomendado |

## Auditoria observada do arquivo

A implementação deve preservar `null` como ausente e não converter automaticamente para zero. No arquivo examinado:

- `total_column_integrated_water_vapour`: 23.412 valores ausentes (cerca de 66,8%). Excluir inicialmente como feature do modelo principal ou tratar com imputação ajustada apenas no treino e acompanhada de indicador de ausência. Não preencher com zero.
- `rain`: 17.687 valores ausentes (cerca de 50,5%). Não usar como rótulo principal sem investigar a semântica e a origem dos nulos.
- `precipitation`: 345 valores ausentes (cerca de 1,0%). É o rótulo inicial mais completo, mas ainda exige exclusão/estratégia explícita para rótulos ausentes.
- `cloud_cover` e as três variáveis por camada têm valores `-1` e `101`, fora da faixa declarada de 0–100%. Tratar como inválidos/ausentes, não como valores reais. A validação deve ser aplicada também a novos dados.
- Nulos e valores inválidos devem ser quantificados por campo e por intervalo de tempo em logs/relatório de ingestão. Não alterar o arquivo original.

Essas observações são específicas deste arquivo. O processo de ingestão deve recalcular os diagnósticos ao receber novos dados.

## Definição do problema de ML

### Alvo recomendado para a primeira versão

Começar com previsão binária de ocorrência de chuva, derivada de `precipitation`:

`chove = 1` quando a precipitação observada no instante/intervalo alvo for maior que um limiar configurável; caso contrário `0`.

O limiar não está especificado pelo JSON e deve ser uma configuração documentada do produto/modelo. Usar `> 0 mm` como baseline técnico inicial é possível, mas pode classificar precipitação residual como chuva relevante. Avaliar e registrar também um limiar operacional (por exemplo, chuva mensurável), acordado com o domínio antes de publicar previsões. Não esconder esse limiar no código.

Uma segunda saída opcional é regressão da quantidade de precipitação (`mm`) no horizonte escolhido. Avaliar separadamente erro em períodos secos e chuvosos e não inferir que uma boa classificação implica boa estimativa de volume.

### Horizonte

O JSON é histórico horário e não define o produto final nem o horizonte desejado. Implementar horizonte como configuração explícita. Para um baseline, prever a ocorrência/quantidade em `t + h`, com `h` em horas, e nunca usar valores do próprio alvo ou do futuro como features. Se o produto quiser acumulado em várias horas, construir o rótulo somando precipitação somente dentro da janela futura declarada.

Não afirmar que o modelo prevê vários dias à frente apenas porque há quatro anos de dados. Horizonte, atualização, latência e frequência de previsão devem estar definidos pela aplicação.

## Preparação e validação dos dados

1. Ler `params` e `response`; validar coordenadas, datas, unidades, campos esperados, tipos e comprimentos dos arrays.
2. Converter `time` em timestamps timezone-aware UTC. Ordenar e verificar duplicatas, lacunas e frequência horária. Não usar o índice da linha como substituto da hora.
3. Transformar arrays em registros tabulares alinhados por timestamp. Manter fonte, coordenadas, timezone e versão do esquema como metadados do dataset.
4. Preservar nulos. Aplicar limites físicos/sintáticos por variável (por exemplo, humidades e cobertura de nuvens em 0–100%; direção em 0–360°; precipitação não negativa), mantendo regra de qualidade auditável.
5. Não treinar com rótulo ausente. Para features ausentes, comparar exclusão de campo com imputação treinada somente no conjunto de treino e coluna indicadora de ausência. Imputer/scaler, se usados, devem ser ajustados dentro de cada fold, nunca antes da separação temporal.
6. Para `wind_direction_10m`, usar codificação cíclica (`sin`/`cos`) em vez de tratar graus como escala linear simples.
7. Derivar hora do dia e dia do ano como variáveis cíclicas se úteis; considerar sazonalidade anual e diária. Não gerar features usando informação futura.
8. Se gerar lags/rolling windows, calcular exclusivamente com observações anteriores ao instante previsto; usar janelas fechadas no passado. Separar o cálculo por série/localidade.
9. Não misturar a mesma hora, nem linhas derivadas da mesma janela-alvo, entre treino e validação/teste. Evitar validação aleatória (`train_test_split` aleatório), que causa vazamento temporal.

### Particionamento temporal

Usar validação walk-forward/expanding window ou blocos cronológicos. Como exemplo inicial, treinar nos anos mais antigos, validar em um período posterior para seleção e reservar o período final (por exemplo, parte de 2025) como teste intocado. Definir os cortes exatos no código/configuração e documentá-los. Para comparar anos e sazonalidade, reportar resultados por ano, mês/estação e período do dia quando o volume permitir.

O modelo de produção deve ser treinado novamente com o histórico permitido após a seleção; o resultado do teste final não deve ser usado para escolher features/limiares repetidamente.

## Baselines e métricas

Implementar pelo menos:

- Baseline de persistência: repetir a condição/valor recente, adaptado ao horizonte.
- Baseline climatológico: frequência/quantidade típica por época e hora, calculada somente no treino.
- Um modelo tabular simples e regularizado como primeiro modelo de ML. Escolher bibliotecas conforme dependências já adotadas no backend; não adicionar framework pesado sem necessidade comprovada.

Para ocorrência de chuva, reportar confusion matrix, precision, recall, F1, balanced accuracy e PR-AUC; a prevalência de chuva pode tornar acurácia isolada enganosa. Para probabilidades, avaliar Brier score e calibração.

Para quantidade, reportar MAE e RMSE, além de métricas por ocorrência/quantis se aplicável. Comparar todos os modelos aos baselines no mesmo split temporal. Registrar intervalo e contagem de amostras válidas por métrica.

## Contrato de saída do backend

Ajustar nomes/rotas ao padrão existente, mantendo semanticamente estes dados:

- `location`: coordenadas/identificador da localidade;
- `issued_at`: instante de geração em ISO-8601 com fuso;
- `target_time` ou `window_start`/`window_end`: instante/janela prevista;
- `horizon_hours`;
- `rain_probability`: probabilidade entre 0 e 1, quando houver modelo probabilístico;
- `rain_expected`: booleano derivado de limiar configurável;
- `expected_precipitation_mm`: opcional, somente se houver modelo de quantidade;
- `model_version` e `data_version`;
- metadados de qualidade/indisponibilidade quando a previsão não puder ser produzida.

Não retornar uma falsa certeza. Se a API expuser categoria (“baixa/média/alta”), derivá-la de faixas configuráveis e versionadas, mantendo a probabilidade numérica disponível.

## Componentes de implementação esperados

Mapear para a estrutura já existente no projeto. Em termos funcionais, o backend precisa cobrir:

1. **Ingestão/validação:** carregar JSON, verificar esquema, tempo, intervalos, nulos e valores fora de faixa.
2. **Dataset/preparação:** alinhar observações e rótulo por horizonte, produzir features sem vazamento e gerar splits temporais.
3. **Treinamento/avaliação:** treinar baselines e modelo, calcular métricas e persistir artefato versionado junto da configuração/schema.
4. **Inferência:** carregar modelo fora do caminho quente/requisição por cache seguro, validar entradas e produzir saída com metadados.
5. **Integração:** expor serviço/endpoint conforme convenções atuais do backend e retornar erro explícito para dados insuficientes ou incompatíveis.
6. **Observabilidade:** registrar versão, janela dos dados, quantidade de ausentes, tempo de inferência e falhas; não registrar segredos nem payloads pessoais (os dados atuais são meteorológicos).

Separar preparação, treinamento e inferência para que a API não treine o modelo a cada requisição. Não carregar o JSON inteiro nem reprocessar todo o histórico por chamada de previsão. O carregamento/treino deve ocorrer em tarefa offline ou pipeline administrativa; inferência deve usar o artefato já validado.

## Retreinamento e versionamento

- Versionar modelo, features, limiar de chuva, horizonte, intervalo de treino, cortes temporais, métricas, origem/licença dos dados e versão de dependências.
- Garantir compatibilidade do schema entre treinamento e inferência; rejeitar ou tratar campos ausentes explicitamente.
- Definir política de atualização do dataset e retreinamento; o arquivo atual termina em 2025-12-31 e, sozinho, não oferece dados observados de 2026.
- Não interpretar a coluna histórica do modelo meteorológico (`gfs_global`) como observação de estação. Registrar a fonte e suas limitações. Para alegações de desempenho em clima observado real, obter uma fonte de observações independente e alinhar por local, fuso, unidade e intervalo antes de validar.
- Monitorar distribuição de entradas, taxa de chuva prevista/observada quando rótulos recentes estiverem disponíveis, calibração e degradação. Não retreinar automaticamente sem validação e controle de versão.

## Critérios de aceite

- O JSON é validado e convertido em registros sem desalinhamento entre hora e variáveis.
- Nulos e valores inválidos são contabilizados; nenhum nulo crítico vira zero silenciosamente.
- O rótulo e horizonte são configuráveis, explícitos e testados quanto a alinhamento temporal.
- O split é cronológico e as transformações são ajustadas apenas no treino.
- Métricas de teste temporal são comparadas aos baselines e acompanhadas de prevalência/quantidade de amostras.
- Inferência não treina nem reprocessa o histórico a cada chamada.
- Resposta contém hora prevista, horizonte, versão e probabilidade/quantidade suportada pelo modelo.
- Há comportamento definido para entrada insuficiente, modelo indisponível, timestamp inválido ou schema incompatível.

## Fora do escopo definido pelo arquivo

O arquivo, por si só, não define o endpoint, framework de backend, política operacional de alerta, horizonte de negócio, limiar de chuva relevante, rotina de atualização, nem fonte de observações independentes. O implementador deve procurar essas convenções no projeto e registrar decisões necessárias; não inventar regras de produto silenciosamente.
