# Guia do Backend — Maré Alerta

Este documento é o guia vivo de referência para qualquer desenvolvedor trabalhando no backend do **Maré Alerta**. Ele centraliza a arquitetura, o estado atual das funcionalidades, o modelo de banco de dados, os contratos de API e o histórico contínuo de atualizações.

> **Importante para a equipe:** a cada novo commit, refatoração ou adição de funcionalidade, atualize a seção correspondente deste guia e adicione uma entrada no [Histórico Contínuo de Atualizações](#-histórico-contínuo-de-atualizações). Este arquivo é versionado no Git.

---

## 1. Visão Geral e Stack Tecnológica

- **Linguagem:** Java 21 (LTS)
- **Framework Principal:** Spring Boot 4.1.1
- **Gerenciador de Dependências & Build:** Gradle 9.7.1 (Kotlin DSL — `build.gradle.kts`)
- **Persistência & ORM:** Spring Data JPA / Hibernate com PostgreSQL 16
- **Controle de Versão de Banco:** Flyway Migrations ativas: **V1 a V19** (`classpath:db/migration`)
- **Segurança & Autenticação:** Spring Security, JWT stateless (`io.jsonwebtoken:jjwt 0.12.6`), senhas criptografadas com BCrypt
- **Documentação de API:** OpenAPI 3 / Swagger UI (`springdoc-openapi 3.1.1`)
- **Ambiente de Execução:** Docker & Docker Compose (`marealerta_pg` e `marealerta_app`), com pipeline multi-arquitetura no GitHub Actions (GHCR)

---

## 2. Estrutura de Pastas e Módulos

```
src/main/java/com/marealerta/backend/
├── alerta/              # Alertas da Defesa Civil e comunicados de emergência
│   ├── controller/      # Endpoints (/alertas)
│   ├── dto/             # AlertaRequest, AlertaResponse
│   ├── model/           # Alerta, NivelRisco, TipoAlerta, OrigemAlerta
│   ├── repository/      # AlertaRepository
│   └── service/         # Regras de negócio de alertas
├── auth/                # Autenticação e Registro
│   ├── controller/      # Endpoints públicos (/auth/registro, /auth/login)
│   ├── dto/             # LoginRequest, RegisterRequest, TokenResponse
│   └── service/         # AuthService, JwtService, UserDetailsServiceImpl
├── checklist/           # Checklist preventivo de mitigação de danos
│   ├── dto/             # ItemChecklistRequest, ItemChecklistResponse
│   ├── model/           # ItemChecklist, PrioridadeChecklist, StatusChecklist
│   └── repository/      # ItemChecklistRepository
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
| **`telemetria`** | Concluído (V15, V17) | Pendente Service de Ingestão | Modelagem pronta para Open-Meteo. DTOs atendem dashboard e gráficos. |
| **`ocorrencias`** | Concluído (V5, V13, V14) | Service Parcial / Controller Esqueleto | Modelagem pronta com persistência de sinistros e fotos. |
| **`checklist`** | Concluído (V12) | Pendente | Modelagem pronta com prioridades e status. |
| **`alerta`** | Concluído (V4) | Esqueleto | Modelagem base criada. Aguarda definição do gateway de WhatsApp. |
| **`risco` (ML)** | Concluído (V19, Entidade, Repo, DTOs) | Esqueleto | Contratos e persistência implementados com `TIMESTAMPTZ`, `BIGINT`, enum `StatusQualidadeMl` e sem defaults artificiais conforme `docs/IA_PREVISAO_CHUVA_BACKEND.md`. |

---

## 4. Banco de Dados e Histórico de Migrações Flyway

O banco PostgreSQL é gerido estritamente pelo Flyway (`ddl-auto: validate`). Nenhuma alteração manual deve ser feita em banco; toda evolução passa por scripts versionados em `src/main/resources/db/migration/`.

### Migrações Ativas no Projeto (V1 a V19):
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
- **`V11__refactor_estabelecimento_endereco_estruturado.sql`**: Refatoração do endereço estruturado de Belém (`logradouro`, `numero`, `bairro`, `cep`, `municipio`, `uf`).
- **`V12__create_itens_checklist.sql`**: Tarefas de mitigação preventiva com status e prioridade.
- **`V13__refactor_ocorrencias_remove_geo_add_dados_comercio.sql`**: Ocorrência comercial com protocolo, valores estimados de prejuízo, lâmina d'água e horas fechado.
- **`V14__create_ocorrencia_fotos.sql`**: Anexo de fotos de sinistros vinculados a ocorrências.
- **`V15__create_leituras_telemetria.sql`**: Tabela de séries temporais climáticas e de maré com chave idempotente.
- **`V16__normalize_bairro_and_schema_consistency.sql`**: Normalização de bairros e remoção de defaults artificiais.
- **`V17__add_clima_columns_telemetria.sql`**: Colunas da Open-Meteo: `temperatura`, `vento_velocidade` e `vento_direcao`.
- **`V18__fix_usuario_email_soft_delete.sql`**: Índice único parcial ativo para e-mail (`WHERE deleted_at IS NULL`).
- **`V19__create_previsoes_chuva_ml.sql`**: Criação da tabela de previsões de machine learning (`previsoes_chuva_ml`) com chave primária `BIGINT`, tipos `TIMESTAMPTZ`, constraints estritas de probabilidade (0.0 a 1.0) e enum de qualidade, sem defaults artificiais.

### Planejamento de Migrações Futuras (V20 a V23 — Refatoração Adiada):
As seguintes migrações foram desenhadas na análise de arquitetura e estão planejadas para execução em momento oportuno da sprint:
- **`V20` (Limpeza de Colunas e Índices):** Remoção das colunas legadas `data_inicio` e `data_fim` de ocorrências; remoção de índices redundantes (`idx_estabelecimento_bairro`, `idx_ocorrencias_estabelecimento_id`, `idx_checklist_estabelecimento_id`) e criação dos índices compostos alinhados às consultas (`idx_ocorrencias_status_data`, `idx_checklist_estab_ordem`).
- **`V21` (Integridade e Constraints sem DEFAULTs artificiais):** Constraints `CHECK` no PostgreSQL para enums (`tipo_negocio`, `status`, `nivel_alagamento`, `prioridade`, `nivel_risco`, `tipo_alerta`), valores não negativos em ocorrências, validação regex de UF e remoção definitiva de defaults de texto artificial (`'Não informado'`, `'S/N'`, `'00:00:00'`).
- **`V22` (Telemetria Multi-Fonte):** Adição da coluna `fonte` (`OPEN_METEO`, `TABUA_MARE`, `MODELO_ML`) e chave única idempotente `(estacao_codigo, fonte, tipo_registro, data_hora)` para evitar colisão entre previsões da IA e dados observados da estação.
- **`V23` (Concorrência e E-mail Case-Insensitive):** Sequence PostgreSQL `seq_protocolo_ocorrencia` para geração atômica de protocolos sem condição de corrida e índice funcional `LOWER(email)` onde `deleted_at IS NULL`.

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
3. **Contratos DTOs (`com.marealerta.backend.risco.dto`):**
   - `PrevisaoMlRequest`: Entrada para inferência/treinamento com coordenadas de Belém (`-1.4558, -48.5044`) e variáveis auditadas (`temperatura2m`, `umidadeRelativa2m`, `pressaoSuperficie`, `coberturaNuvens`, `velocidadeVento10m`, `direcaoVento10m`, `precipitacaoObservada1h`, `cape`).
   - `PrevisaoMlResponse`: Saída padronizada de inferência com `Instant` e `StatusQualidadeMl`.
   - `RiscoResponse`: Visão consolidada para dashboard e alertas de comerciantes (`nivelRisco`, `probabilidadeChuva`, `nivelMarePrevistoMetros`, `horarioPicoPrevisto`).

---

## 6. Convenções de Código e Boas Práticas

1. **Camada de Dados Estrita:** Nenhuma constraint ou regra de negócio crítica deve existir apenas no Java; use `CHECK`, índices parciais e chaves compostas no banco para garantir consistência quando scripts ou serviços externos (como ML em Python) acessarem a base.
2. **Sem Comentários no Código:** O projeto segue a diretriz estrita de código limpo e autoexplicativo, sem comentários inline em arquivos Java ou SQL.
3. **Imutabilidade de Migrações:** Nunca edite um script Flyway que já foi executado em qualquer ambiente. Crie sempre uma nova migração sequencial para aplicar correções (regra Expand/Contract).
4. **Validação:** DTOs de entrada usam Jakarta Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Email`). Respostas de erro são padronizadas via `ApiExceptionHandler` retornando o record `ApiError`.
5. **Proibição de DEFAULTs Artificiais:** Não utilizar valores padrão sentinela em migrations SQL (`DEFAULT 'Não informado'`, `'S/N'`, etc.) nem duplicar defaults entre código Java (`@Builder.Default`, `@PrePersist`) e banco. Dados ausentes devem ser tratados como `NULL` ou obrigatoriamente exigidos pelo contrato da API.
6. **Padronização de TIMESTAMPTZ:** Todas as novas tabelas, evoluções de timestamps e contratos DTOs devem utilizar `TIMESTAMPTZ` no PostgreSQL e `java.time.Instant` no Java (ISO-8601 UTC). Isso elimina bugs de fuso horário decorrentes de divergências entre máquinas de desenvolvimento locais (Belém GMT-3), contêineres Docker (UTC) e servidores de produção.
7. **Uso de BIGINT para Séries Temporais:** Identificadores de tabelas de telemetria e previsões devem utilizar `BIGINT` (`Long` no Java) para evitar risco de exaustão de sequence em dados de alta frequência horária.
8. **Uso de Enum para Estados Finitos:** Status de qualidade e classificações de risco utilizam tipos enumerados para garantir segurança de tipos e contratos explícitos.

---

## 7. Como Executar e Testar

### Pré-requisitos
- Java 21 LTS
- Docker Desktop ativo

### Inicialização Rápida:
```bash
# 1. Iniciar PostgreSQL
docker compose up -d postgres

# 2. Compilar e Rodar Testes
./gradlew test

# 3. Executar o Backend
./gradlew bootRun
```

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## 📜 Histórico Contínuo de Atualizações

| Data | Autor | Resumo das Mudanças | Impacto |
| :--- | :--- | :--- | :--- |
| **06/10/2026** | AKIRA-NZR | Implementação da camada de dados de ML conforme `docs/IA_PREVISAO_CHUVA_BACKEND.md`: criação da migration `V19__create_previsoes_chuva_ml.sql` (`BIGINT`, `TIMESTAMPTZ`, constraints estritas e sem defaults), entidade `PrevisaoChuvaMl`, repositório `PrevisaoChuvaMlRepository`, enum `StatusQualidadeMl` e tipagem estrita nos DTOs. | Persistência e tipagem de previsões meteorológicas preparadas com segurança temporal e integridade relacional. |
| **05/10/2026** | AKIRA-NZR | Criação do `docs/GUIA_BACKEND.md`. Implementação inicial dos DTOs de ML padronizados com `Instant`/`TIMESTAMPTZ`. Sincronização com `origin/main` (CI/GHCR). | Documentação viva centralizada e alinhamento de timezone do backend. |
| **02/10/2026** | Andr3Dav1d | Workflow CI para publicação de imagens multi-arquitetura no GHCR (`.github/workflows/ci.yml`). | Pipeline de entrega contínua automatizada. |
| **01/10/2026** | AKIRA-NZR | Migrações V17 e V18: adição das colunas climáticas Open-Meteo na telemetria e índice único parcial para soft delete de e-mail. DTOs de telemetria e gráficos. | Suporte a dados meteorológicos e correção do histórico V10. |
