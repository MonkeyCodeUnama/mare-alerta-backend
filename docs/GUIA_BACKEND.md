# Guia do Backend — Maré Alerta

Este documento é o guia vivo de referência para qualquer desenvolvedor trabalhando no backend do **Maré Alerta**. Ele centraliza a arquitetura, o estado atual das funcionalidades, o modelo de banco de dados, os contratos de API e o histórico contínuo de atualizações.

> **Importante para a equipe:** a cada novo commit, refatoração ou adição de funcionalidade, atualize a seção correspondente deste guia e adicione uma entrada no [Histórico Contínuo de Atualizações](#-histórico-contínuo-de-atualizações). Este arquivo é versionado no Git.

---

## 1. Visão Geral e Stack Tecnológica

- **Linguagem:** Java 21 (LTS)
- **Framework Principal:** Spring Boot 4.1.1
- **Gerenciador de Dependências & Build:** Gradle 9.7.1 (Kotlin DSL — `build.gradle.kts`)
- **Persistência & ORM:** Spring Data JPA / Hibernate com PostgreSQL 16
- **Controle de Versão de Banco:** Flyway Migrations ativas: **V1 a V24** (`classpath:db/migration`)
- **Segurança & Autenticação:** Spring Security, JWT stateless (`io.jsonwebtoken:jjwt 0.12.6`), senhas criptografadas com BCrypt
- **Documentação de API:** OpenAPI 3 / Swagger UI (`springdoc-openapi 3.1.1`)
- **Gateway de WhatsApp:** Evolution API v2 (`evoapicloud/evolution-api:latest`, Baileys/WebSocket, REST API com Manager UI e Webhooks)
- **Ambiente de Execução:** Docker & Docker Compose (`marealerta_pg`, `marealerta_app` e `marealerta_whatsapp`), com pipeline multi-arquitetura no GitHub Actions (GHCR)

---

## 2. Estrutura de Pastas e Módulos

```
src/main/java/com/marealerta/backend/
├── alerta/              # Alertas da Defesa Civil e despachos WhatsApp
│   ├── controller/      # Endpoints (/alertas)
│   ├── dto/             # AlertaRequest, AlertaResponse
│   ├── model/           # Alerta, NivelRisco, TipoAlerta, OrigemAlerta, EnvioAlertaWhatsapp, StatusEnvioWhatsapp (Enum)
│   ├── repository/      # AlertaRepository, EnvioAlertaWhatsappRepository
│   └── service/         # AlertaService
├── auth/                # Autenticação e Registro
│   ├── controller/      # Endpoints públicos (/auth/registro, /auth/login)
│   ├── dto/             # LoginRequest, RegisterRequest, TokenResponse
│   └── service/         # AuthService, JwtService, UserDetailsServiceImpl
├── checklist/           # Checklist preventivo de mitigação de danos
│   ├── controller/      # Endpoints (/checklists, /checklists/registrar, /checklists/listar)
│   ├── dto/             # ItemChecklistRequest, ItemChecklistResponse
│   ├── model/           # ItemChecklist, PrioridadeChecklist, StatusChecklist
│   ├── repository/      # ItemChecklistRepository
│   └── service/         # ChecklistService
├── estabelecimento/     # Perfil comercial do comerciante e endereçamento
│   ├── controller/      # Endpoints (/estabelecimento, /estabelecimento/me, /estabelecimento/{id})
│   ├── dto/             # EstabelecimentoRequest, EstabelecimentoAtualizacaoRequest, EstabelecimentoResponse
│   ├── model/           # Estabelecimento, EstabelecimentoHorario, TipoNegocio, Bairro, ConversorBairro
│   ├── repository/      # EstabelecimentoRepository, EstabelecimentoHorarioRepository
│   └── service/         # EstabelecimentoService (Onboarding em 2 etapas)
├── itens/               # Bens vulneráveis a alagamento vinculados ao estabelecimento
│   ├── model/           # ItemCritico
│   └── repository/      # ItensCriticosRepository
├── ocorrencias/         # Sinistros pós-alagamento e anexos
│   ├── controller/      # Endpoints (/ocorrencias)
│   ├── dto/             # OcorrenciaRequest, OcorrenciaResponse, OcorrenciaFotoResponse
│   ├── model/           # Ocorrencia, OcorrenciaFoto, StatusOcorrencia, NivelAlagamento
│   ├── repository/      # OcorrenciaRepository, OcorrenciaFotoRepository
│   └── service/         # OcorrenciaService (geração de protocolo estável)
├── risco/               # Inteligência e Previsão Climática/ML
│   ├── controller/      # Endpoints (/risco/atual)
│   ├── dto/             # PrevisaoMlRequest, PrevisaoMlResponse, RiscoResponse
│   ├── model/           # PrevisaoChuvaMl, StatusQualidadeMl (Enum)
│   ├── repository/      # PrevisaoChuvaMlRepository
│   └── service/         # RiscoService, MlApiCliente
├── shared/              # Configurações globais transversais
│   ├── config/          # RestClientConfig
│   ├── security/        # SecurityConfig, JwtAuthFilter, CorsConfig
│   └── web/             # ApiError, ApiExceptionHandler, HomeController
├── telemetria/          # Dados meteorológicos e cotas de maré (Open-Meteo)
│   ├── dto/             # LeituraTelemetriaRequest, LeituraTelemetriaResponse, TelemetriaDashboardResponse, TelemetriaPontoGraficoDTO
│   ├── model/           # LeituraTelemetria, TipoRegistroTelemetria
│   └── repository/      # LeituraTelemetriaRepository
└── usuario/             # Credenciais e vínculos de usuários
    ├── dto/             # UsuarioResponse
    ├── model/           # Usuario (implementa UserDetails do Spring Security)
    └── repository/      # UsuarioRepository
```

---

## 3. Estado Atual dos Módulos

| Módulo | Camada de Dados (DB, Entidade, Repo, DTO) | Camada de Negócio / API (Service, Controller) | Situação Atual |
| :--- | :---: | :---: | :--- |
| **`auth`** | Concluído | Concluído | 100% funcional. Registro emite JWT e cria estabelecimento inicial. |
| **`estabelecimento`** | Concluído | Concluído | 100% funcional. Suporta onboarding em 2 etapas com aliases React. |
| **`telemetria`** | Concluído (V15, V17, V23) | Concluído | 100% funcional. `OpenMeteoClient` sincroniza 10 parâmetros atmosféricos de Belém, mapeia com tolerância a campos futuros (`@JsonIgnoreProperties`) e expõe endpoints `/telemetria/dashboard`, `/telemetria/grafico`, `/telemetria/sincronizar` e `/telemetria/historico`. |
| **`ocorrencias`** | Concluído (V5, V13, V14) | Concluído | 100% funcional. `OcorrenciaService` gera protocolo `OC-{estab}-{quant}`, persiste sinistros e gerencia upload de fotos em disco. |
| **`checklist`** | Concluído (V12) | Concluído | 100% funcional. `ChecklistService` e `ChecklistController` com suporte a `/checklists`, `/checklists/registrar` e `/checklists/listar`, criação, atualização e listagem de itens preventivos por estabelecimento com prioridades (`ALTA`, `MEDIA`, `BAIXA`), e 11 testes automatizados. |
| **`alerta`** | Concluído (V4, V20) | Concluído | 100% funcional. `AlertaService`, `AlertaController` e `EvolutionApiClient` conectados à Evolution API v2 para emissão individual e broadcast por bairro. Sanitização e formatação E.164 (`55919XXXXXXXX`), registro e rastreio de auditoria em `envios_alerta_whatsapp` com enums de status e controle de tentativas. |
| **`risco` (ML)** | Concluído (V19, Entidade, Repo, DTOs) | Concluído | 100% funcional. Integrado ao microserviço FastAPI (`mare-alerta-ia`), com `feature_time` truncado em UTC, consumo de parâmetros atmosféricos reais da telemetria, tratamento de `window_end`, e fallback gracioso (`DEGRADADO`) em caso de indisponibilidade da IA. |

---

## 4. Banco de Dados e Histórico de Migrações Flyway

O banco PostgreSQL é gerido estritamente pelo Flyway (`ddl-auto: validate`). Nenhuma alteração manual deve ser feita em banco; toda evolução passa por scripts versionados em `src/main/resources/db/migration/`.

### Migrações Ativas no Projeto (V1 a V24):
- **`V1__create_estabelecimento.sql`**: Tabela base de comércios.
- **`V2__create_usuario.sql`**: Tabela de usuários e credenciais vinculadas a estabelecimentos.
- **`V3__create_estabelecimento_horarios.sql`**: Grade semanal de funcionamento (`dia_semana` 1..7).
- **`V4__create_alerta.sql`**: Alertas e comunicados da Defesa Civil.
- **`V5__create_ocorrencias.sql`**: Criação da tabela de sinistros pós-alagamento.
- **`V6__correction_soft_delete.sql`**: Adiciona `deleted_at` para soft delete em estabelecimentos.
- **`V7__create_itens_criticos.sql`**: Relação de bens vulneráveis do comerciante (freezers, motores, estoque).
- **`V8__alter_ocorrencias_data_fim_nullable.sql`**: Permite ocorrência em andamento sem data final.
- **`V9__create_index_documento_estabelecimento.sql`**: Índice único parcial para documento ativo.
- **`V10__fix_usuario_email_soft_delete.sql`**: Preservada como arquivo vazio (checksum = 0) para compatibilidade histórica de ambientes.
- **`V11__refactor_estabelecimento_endereco_estruturado.sql`**: Refatoração do endereço estruturado de Belém (`logradouro`, `numero`, `bairro`, `cep`, `municipio`, `uf`, `telefone`).
- **`V12__create_itens_checklist.sql`**: Tarefas de mitigação preventiva com status e prioridade.
- **`V13__refactor_ocorrencias_remove_geo_add_dados_comercio.sql`**: Ocorrência comercial com protocolo, valores estimados de prejuízo, lâmina d'água e horas fechado.
- **`V14__create_ocorrencia_fotos.sql`**: Anexo de fotos de sinistros vinculados a ocorrências.
- **`V15__create_leituras_telemetria.sql`**: Tabela de séries temporais climáticas e de maré com chave idempotente.
- **`V16__normalize_bairro_and_schema_consistency.sql`**: Normalização de bairros e remoção de defaults artificiais.
- **`V17__add_clima_columns_telemetria.sql`**: Colunas da Open-Meteo: `temperatura`, `vento_velocidade` e `vento_direcao`.
- **`V18__fix_usuario_email_soft_delete.sql`**: Índice único parcial ativo para e-mail (`WHERE deleted_at IS NULL`).
- **`V19__create_previsoes_chuva_ml.sql`**: Criação da tabela de previsões de machine learning (`previsoes_chuva_ml`) com chave primária `BIGINT`, tipos `TIMESTAMPTZ`, constraints estritas de probabilidade (0.0 a 1.0) e enum de qualidade, sem defaults artificiais.
- **`V20__create_envios_alerta_whatsapp.sql`**: Tabela de despacho e rastreio de notificações de WhatsApp (`envios_alerta_whatsapp`) vinculada ao alerta e estabelecimento, com status em enum, controle de tentativas e `TIMESTAMPTZ`.
- **`V21__cleanup_colunas_e_indices.sql`**: Remoção de colunas legadas (`data_inicio`, `data_fim`) em `ocorrencias`, expurgo de índices redundantes e adição de índices compostos de alta performance (`idx_ocorrencias_status_data`, `idx_checklist_estab_ordem`).
- **`V22__add_check_constraints_integridade.sql`**: Endurecimento do PostgreSQL com constraints `CHECK` para todos os enums do sistema, valores numéricos não-negativos, integridade de soft delete e preservação de documento opcional para comércios informais.
- **`V23__add_fonte_telemetria.sql`**: Adição da coluna `fonte` (`OPEN_METEO`, `TABUA_MARE`) na tabela `leituras_telemetria` e chave única idempotente multi-fonte `(estacao_codigo, fonte, tipo_registro, data_hora)`.
- **`V24__email_case_insensitive.sql`**: Índice único funcional `LOWER(email)` onde `deleted_at IS NULL` para garantir busca e unicidade insensível a maiúsculas/minúsculas.

---

## 5. Módulo de Inteligência e Machine Learning (`risco`)

Conforme a especificação em `docs/IA_PREVISAO_CHUVA_BACKEND.md`, o módulo `risco` implementa a camada de dados e contratos de previsão de chuva para Belém:

1. **Entidade de Persistência (`PrevisaoChuvaMl`):**
   - Mapeia a tabela `previsoes_chuva_ml` via JPA.
   - Chave primária `BIGINT` (`Long`) para suportar alto volume de séries temporais.
   - Campos temporais com `Instant` (UTC/`TIMESTAMPTZ`): `instanteEmissao`, `instanteAlvo`, `criadoEm`.
   - `statusQualidade`: Tipado com o enum `StatusQualidadeMl` (`OK`, `DEGRADADO`, `DADOS_INSUFICIENTES`, `INDISPONIVEL`).
   - Sem valores default artificiais no Java ou SQL.
2. **Repositório JPA (`PrevisaoChuvaMlRepository`):**
   - Métodos otimizados para busca de previsões por janela de horizonte (`buscarPrevisoesJanela`).
   - Busca da última previsão emitida válida para um determinado instante (`buscarUltimaPrevisaoValida`).
3. **Contratos DTOs e Alinhamento com a API de IA (`mare-alerta-ia`):**
   - O microserviço preditivo em Python FastAPI expõe a rota `POST /v1/predict` aceitando o esquema `rain-hourly-v1`.
   - `PrevisaoMlRequest`: Transmite as 20 features atmosféricas de Belém em formato numérico e dicionário de unidades. **Requisito estrito do modelo:** o campo `feature_time` é truncado para a hora cheia em UTC (`OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS)`), evitando erro HTTP 422 (`time != time.floor('h')`).
   - `PrevisaoMlResponse`: Mapeia a resposta da IA com `@JsonIgnoreProperties(ignoreUnknown = true)` para tolerar metadados adicionais (`decision_threshold`, `data_version`, `target_source`, `quality`) e `@JsonAlias({"window_end", "target_time"})` para capturar a janela preditiva.
   - `RiscoResponse`: Visão consolidada para dashboard e alertas de comerciantes (`nivelRisco`, `probabilidadeChuva`, `nivelMarePrevistoMetros`, `horarioPicoPrevisto`), com fuso horário fixado em `America/Belem`.
4. **Resiliência e Fallback Gracioso:**
   - Em caso de falha de comunicação ou timeout com o serviço de Machine Learning, o `RiscoService` não interrompe a aplicação nem retorna HTTP 500.
   - Aciona o método `criarPrevisaoFallback(...)` que estima probabilidade e precipitação a partir dos dados observados da telemetria Open-Meteo.
   - A previsão é persistida na tabela `previsoes_chuva_ml` com `statusQualidade = StatusQualidadeMl.DEGRADADO` e versão `fallback-heuristic-v1`, mantendo a rastreabilidade e integridade para a Defesa Civil.

---

## 6. Integração e Rastreio de Alertas via WhatsApp (Evolution API)

### A. Escolha da Tecnologia e Análise de Trade-offs
Para o envio de comunicados emergenciais de alagamento aos comerciantes cadastrados, foi avaliado o panorama de APIs disponíveis:

| Opção Avaliada | Custo | Burocracia | Infraestrutura | Veredito para o Maré Alerta |
| :--- | :---: | :---: | :---: | :--- |
| **Evolution API v2** | **R$ 0 (Open Source)** | **Nenhuma** | Container Docker local | **Adotada.** Integração direta via Docker Compose, sem cobrança por disparo, leve (Baileys/WebSocket) e compatível com o ecossistema do projeto. |
| **Meta Cloud API Oficial** | Grátis até 1.000 msgs/mês, depois R$ 0,035/msg | Alta (WABA, Meta Business, aprovação prévia de Templates) | Nuvem da Meta | Rejeitada para o MVP acadêmico/TCC devido à burocracia de validação de CNPJ e aprovação de modelos de mensagem. |
| **CallMeBot** | R$ 0 | Baixa | Nuvem de terceiros | Rejeitada para produção. Exige que cada comerciante adicione o bot e requisite uma API key individual, inviabilizando uso público. |

### B. Diretrizes Operacionais e Segurança (Anti-Ban)
1. **Número Dedicado:** O sistema **nunca** deve utilizar o número pessoal de desenvolvedores. É obrigatório o uso de um chip pré-pago dedicado (DDD 91) ativado exclusivamente para o bot do Maré Alerta.
2. **Autenticação por QR Code:** A Evolution API sobe via Docker, expõe um painel na porta `8081` onde o operador escaneia o QR Code uma única vez via *"Aparelhos Conectados"*. As credenciais da sessão são salvas no volume Docker persistente `evolution_instances`.
3. **Controle de Fila e Intervalo:** Para evitar bloqueio algorítmico do WhatsApp por envio simultâneo, o serviço de envio deve implementar espaçamento de 1 a 3 segundos entre mensagens quando alertar múltiplos estabelecimentos no mesmo bairro.

### C. Modelagem Relacional de Despacho (`envios_alerta_whatsapp`)
Para garantir **idempotência** (se o servidor cair durante o envio para 50 lojas, saber exatamente quem já recebeu e quem ainda está pendente) e fornecer **auditoria para a Defesa Civil**, a tabela `envios_alerta_whatsapp` foi criada na migração `V20`:

* **`alerta_id`:** Referência ao evento de alerta emitido.
* **`estabelecimento_id`:** Comércio destinatário.
* **`telefone_destinatario`:** Número no formato internacional E.164 (`55919XXXXXXXX`).
* **`status_envio`:** Enum `StatusEnvioWhatsapp` (`PENDENTE`, `PROCESSANDO`, `ENVIADO`, `FALHA`).
* **`whatsapp_message_id`:** Identificador retornado pelo gateway para conferência.
* **`tentativas`:** Contador de retentativas para mensagens com falha de conexão.
* **`criado_em` / `enviado_em`:** Timestamps padronizados em `TIMESTAMPTZ` (`Instant`).

### D. Configuração do Ambiente
* **Docker Compose:** Serviço `evolution_api` rodando na imagem `evoapicloud/evolution-api:latest`, isolado no schema `evolution` do PostgreSQL (`marealerta_dev`), com volume `evolution_instances` e limite de 384MB de RAM.
* **Spring Boot (`application.yaml`):**
  ```yaml
  whatsapp:
    evolution:
      url: ${WHATSAPP_API_URL:http://localhost:8081}
      api-key: ${WHATSAPP_API_KEY:marealerta_secret_key_2026}
      instancia: ${WHATSAPP_INSTANCIA:marealerta_belem}
  ```

### E. Procedimento Operacional: Conexão via QR Code e Teste de Disparo
1. **Acessar o Gerenciador Web:** Abra no navegador `http://localhost:8081/manager`.
2. **Autenticar:** Informe a API Key global configurada: `marealerta_secret_key_2026`.
3. **Escanear QR Code:** Na instância `marealerta_belem`, abra a visualização do QR Code. No WhatsApp do celular: *Configurações* (ou 3 pontinhos) ➡️ *Aparelhos Conectados* ➡️ *Conectar um aparelho* e escaneie o código.
4. **Verificar Status de Conexão:**
   ```powershell
   Invoke-RestMethod -Uri "http://localhost:8081/instance/connectionState/marealerta_belem" `
     -Headers @{ "apikey" = "marealerta_secret_key_2026" }
   ```
   Deve retornar `state: "open"`.
5. **Disparar Mensagem de Teste:**
   ```powershell
   $body = @{
       number = "5591987654321"
       text = "🚨 *MARÉ ALERTA — DEFESA CIVIL*`n`nRisco ALTO de alagamento no bairro Campina às 17:40."
   } | ConvertTo-Json

   Invoke-RestMethod -Uri "http://localhost:8081/message/sendText/marealerta_belem" `
     -Method Post `
     -Headers @{ "apikey" = "marealerta_secret_key_2026" } `
     -ContentType "application/json" `
     -Body $body
   ```

---

## 7. Arquitetura de Segurança, JWT, Refresh Token e Rate Limiting

### A. Fluxo de Autenticação e Renovação de Sessão (JWT & Refresh Token)
O backend adota arquitetura de segurança stateless com dois níveis de token:
1. **Access Token:**
   - Emitido no login e registro (`POST /auth/login` e `POST /auth/registro`).
   - Contém o claim `type: "ACCESS"`, sujeito do usuário e tempo de vida curto/médio (`jwt.expiration`, padrão 2h30 ou 15m configurável).
   - Enviado no cabeçalho `Authorization: Bearer <token>` para acesso a rotas privadas.
2. **Refresh Token:**
   - Emitido juntamente com o access token, contendo o claim estrito `type: "REFRESH"` e expiração de longa duração (`jwt.refresh-expiration`, padrão 7 dias).
   - Utilizado exclusivamente no endpoint `POST /auth/refresh` com payload `{"refreshToken": "..."}`.
   - O `JwtAuthFilter` rejeita sumariamente refresh tokens enviados diretamente em rotas protegidas normais, impedindo uso indevido.
   - Retorna novo par de tokens (`token` e `refreshToken`).

### B. Verificação Estrita de Usuário e Estabelecimento no Login
Para impedir acesso de contas suspensas ou comércios desativados:
- O `AuthService` valida se `user.getDeletedAt() == null`.
- Valida se o estabelecimento vinculado está ativo (`estabelecimento.getAtivo() == true`).
- Tentativas de login ou renovação de refresh token para usuários excluídos ou comércios inativos são barradas com `HTTP 401 Unauthorized` (`InvalidCredentialsException`).

### C. Rate Limiting com Bucket4j e Cache Caffeine
Para proteção contra força bruta em credenciais, scraping e sobrecarga da infraestrutura:
- Implementado em `RateLimitFilter` utilizando algoritmo Token Bucket (`com.bucket4j:bucket4j-core`) armazenado em memória com expiração LRU via `com.github.benmanes.caffeine:caffeine`.
- Limitação calculada por IP de origem do cliente (com suporte a proxies reversos via header `X-Forwarded-For`).
- Parâmetros configuráveis via `application.yaml` e `.env`:
  - `security.rate-limit.capacity`: 60 requisições
  - `security.rate-limit.refill-tokens`: 60 tokens
  - `security.rate-limit.refill-duration-seconds`: 60 segundos
- Ao exceder o balde, responde imediatamente com `HTTP 429 Too Many Requests`, cabeçalho `Retry-After: {segundos}` e payload JSON padronizado.
- Rotas do Swagger UI e OpenAPI são isentas do filtro para permitir documentação fluida.

### D. Hardening de CORS
- Configurado via `CorsConfig` consumindo `app.cors.allowed-origins` (`CORS_ALLOWED_ORIGINS`).
- Origens padrão autorizadas: `http://localhost:3000`, `http://localhost:5173`, `http://127.0.0.1:5173`.
- Suporte a credenciais (`allowCredentials: true`), métodos `GET, POST, PUT, DELETE, OPTIONS, PATCH` e tempo de cache preflight de 3600 segundos.

### E. Matriz de Autorização de Rotas (SecurityConfig)
| Rota / Prefixo | Método | Acesso | Descrição |
| :--- | :---: | :---: | :--- |
| `/`, `/swagger-ui/**`, `/v3/api-docs/**` | GET | Público | Documentação interativa OpenAPI/Swagger |
| `/auth/registro`, `/auth/login`, `/auth/refresh` | POST | Público | Registro, login e renovação de token |
| `/telemetria/**` | Qualquer | Público | Dados em tempo real das marés, gráficos, histórico e sincronização manual |
| `/risco/**` | GET | Público | Consulta de previsão de alagamento e IA |
| `/alertas` e `/alertas/**` | GET | Público | Consulta e histórico público de alertas |
| `OPTIONS /**` | OPTIONS | Público | Requisições de preflight do CORS |
| `/alertas` e `/alertas/bairro` | POST | Autenticado | Emissão e broadcast de novos alertas |
| `/checklists/**` | Qualquer | Autenticado | Planejamento preventivo do estabelecimento |
| `/ocorrencias/**` | Qualquer | Autenticado | Registro de sinistros e anexo de fotos |
| `/estabelecimento/**` | Qualquer | Autenticado | Perfil comercial, horários e itens críticos |

---

## 8. Convenções de Código e Boas Práticas

1. **Camada de Dados Estrita:** Nenhuma constraint ou regra de negócio crítica deve existir apenas no Java; use `CHECK`, índices parciais e chaves compostas no banco para garantir consistência quando scripts ou serviços externos (como ML em Python ou Webhooks da Evolution API) acessarem a base.
2. **Sem Comentários no Código:** O projeto segue a diretriz estrita de código limpo e autoexplicativo, sem comentários inline em arquivos Java ou SQL.
3. **Injeção de Dependências via Lombok:** Todos os Services e Controllers utilizam `@RequiredArgsConstructor` sobre atributos `private final`, eliminando construtores manuais repetitivos e anotações `@Autowired` desnecessárias. Classes com `@Value` em parâmetros mantêm construtores explícitos.
4. **Imports Limpos e Sem Wildcards:** Proibido o uso de `import .*`. Todos os imports devem ser explícitos e qualquer import não utilizado deve ser removido.
5. **Imutabilidade de Migrações:** Nunca edite um script Flyway que já foi executado em qualquer ambiente. Crie sempre uma nova migração sequencial para aplicar correções (regra Expand/Contract).
6. **Validação:** DTOs de entrada usam Jakarta Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Email`). Respostas de erro são padronizadas via `ApiExceptionHandler` retornando o record `ApiError`.
7. **Proibição de DEFAULTs Artificiais:** Não utilizar valores padrão sentinela em migrations SQL (`DEFAULT 'Não informado'`, `'S/N'`, etc.) nem duplicar defaults entre código Java (`@Builder.Default`, `@PrePersist`) e banco. Dados ausentes devem ser tratados como `NULL` ou obrigatoriamente exigidos pelo contrato da API.
8. **Padronização de TIMESTAMPTZ:** Todas as novas tabelas, evoluções de timestamps e contratos DTOs devem utilizar `TIMESTAMPTZ` no PostgreSQL e `java.time.Instant` no Java (ISO-8601 UTC). Isso elimina bugs de fuso horário decorrentes de divergências entre máquinas de desenvolvimento locais (Belém GMT-3), contêineres Docker (UTC) e servidores de produção.
9. **Uso de BIGINT para Séries Temporais:** Identificadores de tabelas de telemetria, previsões e despachos de mensagens utilizam `BIGINT` (`Long` no Java) para evitar risco de exaustão de sequence.
10. **Uso de Enum para Estados Finitos:** Status de qualidade, status de envio de WhatsApp e classificações de risco utilizam tipos enumerados para garantir segurança de tipos e contratos explícitos.
11. **Gestão de Segredos e `.env`:** Arquivos `.env` e variações locais são estritamente ignorados no `.gitignore`. O repositório mantém apenas o `.env.example` como gabarito público de configuração. Em ambiente de produção, segredos como `EVOLUTION_API_KEY` e `JWT_KEY` devem ser injetados via variáveis de ambiente da infraestrutura, sobrepondo os valores de fallback de desenvolvimento.

---

## 9. Como Executar e Testar

### Pré-requisitos
- Java 21 LTS
- Docker Desktop ativo

### Inicialização Rápida:
```bash
# 1. Iniciar PostgreSQL e Evolution API
docker compose up -d postgres evolution_api

# 2. Compilar e Rodar Testes
./gradlew test

# 3. Executar o Backend
./gradlew bootRun
```

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Painel Evolution API (WhatsApp):** `http://localhost:8081/manager`

---

## 📜 Histórico Contínuo de Atualizações

| Data | Autor | Resumo das Mudanças | Impacto |
| :--- | :--- | :--- | :--- |
| **12/10/2026** | AKIRA-NZR | Conclusão da Etapa 2 da telemetria: expansão do `OpenMeteoClient` para 10 variáveis atmosféricas superficiais em tempo real com anotação `@JsonIgnoreProperties(ignoreUnknown = true)`, exposição de endpoints `/telemetria/grafico`, `/telemetria/sincronizar` e `/telemetria/historico`, testes unitários atualizados em `OpenMeteoClientTest`, `TelemetriaControllerTest` e `TelemetriaServiceTest`, e smoke test real validado contra a API Open-Meteo para Belém (-1.4558, -48.5044). | Alimentação fidedigna em tempo real do dashboard e do modelo de IA de chuva, cobertura total de endpoints e tolerância a novos atributos da API meteorológica. |
| **12/10/2026** | AKIRA-NZR | Execução das migrações Flyway `V21` a `V24`: limpeza de colunas legadas em `ocorrencias` (`data_inicio`, `data_fim`), expurgo de índices redundantes e adição de índices compostos (`idx_ocorrencias_status_data`, `idx_checklist_estab_ordem`). Endurecimento do PostgreSQL com constraints `CHECK` para todos os enums do sistema e valores numéricos não negativos (`V22`). Suporte a comércios informais sem CNPJ (`documento` opcional). Adição de multi-fonte na telemetria com coluna `fonte` (`OPEN_METEO`, `TABUA_MARE`) e chave única idempotente (`V23`). Normalização e índice funcional case-insensitive `LOWER(email)` (`V24`). Expansão da suíte de testes unitários com criação de `EstabelecimentoServiceTest`, `EstabelecimentoControllerTest`, `UserDetailsServiceImplTest`, `OpenMeteoClientTest` e `MlApiClienteTest`. | Consistência relacional estrita, prevenção contra dados órfãos ou inconsistentes, suporte a trabalhadores informais e 100% de cobertura de testes unitários em todos os módulos. |
| **10/10/2026** | AKIRA-NZR | Sincronização e integração do módulo `checklist` (`ChecklistService`, `ChecklistController`, 11 testes automatizados). Implementação de segurança avançada com JWT Refresh Token (`POST /auth/refresh`), separação estrita de claims `ACCESS` vs `REFRESH`, verificação de usuários ativos e estabelecimentos não-deletados no login. Implementação de Rate Limiting por IP via Bucket4j 8 e Caffeine (`RateLimitFilter` retornando HTTP 429 e `Retry-After`). Hardening de CORS configurável via `CORS_ALLOWED_ORIGINS`. Correção de syntax de `@Value` e rotas na Evolution API e RestClient. Padronização de injeção `@RequiredArgsConstructor`, remoção de wildcard imports e limpeza de comentários em código. Atualização do `.env.example` e documentação viva. | Camada de segurança e autenticação robusta, proteção contra ataques de força bruta, prevenção de vazamento de tokens e alinhamento completo com o módulo preventivo de checklist. |
| **08/10/2026** | AKIRA-NZR | Implementação completa da integração com WhatsApp via Evolution API v2 (`EvolutionApiClient`, `AlertaService`, `AlertaController`) com formatação E.164 (`5591...`), rastreio e auditoria em `envios_alerta_whatsapp` (status `PENDENTE`, `ENVIADO`, `FALHA` e tentativas) e suporte a broadcast por bairro. Aprimoramento da conexão Open-Meteo com requisição e mapeamento de 13 parâmetros atmosféricos superficiais em tempo real para alimentação direta do modelo de Machine Learning (`rain-hourly-v1`). Adição de 18 novos testes automatizados (totalizando 40 testes de unidade e controller com 100% de sucesso). | Sistema de alerta operacional em tempo real integrado ao WhatsApp e IA, eliminando dados climáticos simulados e garantindo auditabilidade da Defesa Civil. |
| **08/10/2026** | AKIRA-NZR | Alinhamento do contrato do microserviço de ML (`mare-alerta-ia`), com truncamento estrito de `feature_time` para hora cheia UTC, anotação `@JsonIgnoreProperties` e mapeamento de `window_end` em `PrevisaoMlResponse`. Implementação de fallback gracioso no `RiscoService` com status `DEGRADADO` para tolerância a falhas na IA. Definição do envio de alertas WhatsApp associado aos telefones de `Estabelecimento.telefone` por bairro. Criação de 22 testes unitários automatizados para `ocorrencias`, `risco` e `telemetria`. | Robustez operacional de ponta a ponta: tolerância a falhas na IA, conformidade de contratos e cobertura de testes automatizados com 100% de sucesso. |
| **06/10/2026** | AKIRA-NZR | Proteção estrita de segredos locais: adição de `.env` ao `.gitignore`, criação do modelo `.env.example` e documentação de boas práticas de variáveis de ambiente. | Blindagem contra vazamento de credenciais e segurança de segredos em produção. |
| **06/10/2026** | AKIRA-NZR | Integração da Evolution API v2 (`evoapicloud/evolution-api:latest`) no `docker-compose.yml` e `application.yaml`, isolada no schema `evolution`. Criação da migração `V20__create_envios_alerta_whatsapp.sql`, entidade `EnvioAlertaWhatsapp`, enum `StatusEnvioWhatsapp` e repositório `EnvioAlertaWhatsappRepository`. Documentação completa de procedimentos de QR Code, testes e anti-ban. | Capacidade de despacho de mensagens no WhatsApp com auditoria, controle de fila e tolerância a falhas. |
| **06/10/2026** | AKIRA-NZR | Implementação da camada de dados de ML conforme `docs/IA_PREVISAO_CHUVA_BACKEND.md`: migration `V19__create_previsoes_chuva_ml.sql`, entidade `PrevisaoChuvaMl`, repositório `PrevisaoChuvaMlRepository`, enum `StatusQualidadeMl` e tipagem estrita nos DTOs. | Persistência e tipagem de previsões meteorológicas com segurança temporal e integridade relacional. |
| **05/10/2026** | AKIRA-NZR | Criação do `docs/GUIA_BACKEND.md`. Implementação inicial dos DTOs de ML padronizados com `Instant`/`TIMESTAMPTZ`. Sincronização com `origin/main` (CI/GHCR). | Documentação viva centralizada e alinhamento de timezone do backend. |
| **02/10/2026** | Andr3Dav1d | Workflow CI para publicação de imagens multi-arquitetura no GHCR (`.github/workflows/ci.yml`). | Pipeline de entrega contínua automatizada. |
| **01/10/2026** | AKIRA-NZR | Migrações V17 e V18: adição das colunas climáticas Open-Meteo na telemetria e índice único parcial para soft delete de e-mail. DTOs de telemetria e gráficos. | Suporte a dados meteorológicos e correção do histórico V10. |


