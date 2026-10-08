# Maré Alerta — Backend

API RESTful do ecossistema **Maré Alerta**, desenvolvida em **Java 21** e **Spring Boot**. O sistema provê inteligência preditiva, monitoramento hidrológico e proteção patrimonial para comerciantes localizados em áreas de cota crítica da bacia hidrográfica de Belém/PA.

---

## 🛠️ Stack Tecnológica

- **Linguagem & Framework:** Java 21 (LTS) & Spring Boot 4.x
- **Persistência & ORM:** Spring Data JPA / Hibernate com PostgreSQL 16
- **Versionamento de Banco:** Flyway Migrations (V1 a V20)
- **Segurança:** Spring Security, JWT (HMAC-SHA256) e criptografia de senhas com BCrypt
- **Gateway de WhatsApp:** Evolution API v2 (Open-source, Baileys via WebSocket)
- **Documentação Interativa:** OpenAPI 3 / Swagger UI
- **Build & Automação:** Gradle 9.7.1
- **Conteinerização:** Docker & Docker Compose

---

## 🏛️ Arquitetura do Banco de Dados (3FN)

O banco de dados foi modelado seguindo a **3ª Forma Normal (3FN)**, com integridade referencial, índices B-Tree estratégicos, índices parciais para soft delete e catálogo oficial de bairros:

```
┌──────────────────┐       1:1       ┌──────────────────┐
│     usuario      │─────────────────│  estabelecimento │
└──────────────────┘                 └────────┬─────────┘
                                              │ 1:N
               ┌──────────────────────────────┼──────────────────────────────┐
               ▼                              ▼                              ▼
┌──────────────────────────────┐ ┌──────────────────────────────┐ ┌──────────────────────────────┐
│   estabelecimento_horarios   │ │        itens_criticos        │ │       itens_checklist        │
│ (dias 1..7, abertura, fecha) │ │   (maquinário, estoques)     │ │   (tarefas de mitigação)     │
└──────────────────────────────┘ └──────────────────────────────┘ └──────────────────────────────┘
```

### Principais Entidades e Tabelas:
- **`usuario` (V2, V18):** Credenciais, vínculo ao estabelecimento e índice único parcial para e-mails ativos (`WHERE deleted_at IS NULL`), garantindo integridade e reutilização de e-mail em soft delete.
- **`estabelecimento` (V1, V11, V16):** Perfil comercial com endereço estruturado (`logradouro`, `numero`, `bairro`, `cep`, `municipio`, `uf`), tipo de negócio e soft delete (`deleted_at`), sem dependência de defaults artificiais.
- **`bairro` (Enum oficial):** Catálogo com os bairros oficiais de Belém (Campina, Umarizal, Marco, Cidade Velha, Jurunas, etc.), com índice B-Tree e mapeamento estrito.
- **`estabelecimento_horarios` (V3):** Grade de funcionamento semanal normalizada por dia da semana (`dia_semana` 1..7) e horários (`TIME`).
- **`itens_criticos` (V7):** Relação de bens vulneráveis à cota de alagamento (freezers, motores, estoque no chão, computadores).
- **`itens_checklist` (V12):** Itens de verificação e checklist preventivo de maré alta com prioridades e status.
- **`ocorrencias` & `ocorrencia_fotos` (V13, V14, V16):** Registro oficial de sinistros pós-alagamento com protocolo estável, suporte a rascunhos, dados comerciais e anexos de fotos.
- **`leituras_telemetria` (V15, V17):** Histórico e previsões climáticas com nível de maré, precipitação e variáveis da Open-Meteo (temperatura, velocidade e direção do vento).
- **`alertas` (V4):** Comunicações de emergência e cotas de alerta emitidas pela Defesa Civil.
- **`previsoes_chuva_ml` (V19):** Persistência de previsões geradas pelo modelo de IA com `BIGINT`, `TIMESTAMPTZ`, constraints de probabilidade e enum de qualidade (`StatusQualidadeMl`).
- **`envios_alerta_whatsapp` (V20):** Despacho, controle de fila e histórico de mensagens enviadas via WhatsApp para comércios locais, com status em enum (`StatusEnvioWhatsapp`) e idempotência.

---

## 🌤️ Modelagem de Telemetria Climática (Open-Meteo)

O ecossistema integra dados meteorológicos da API pública **Open-Meteo** para o município de Belém/PA (`latitude: -1.4558`, `longitude: -48.4902`):
- **Variáveis de Clima:** Precipitação horária (`precipitacao` em mm/h), temperatura ambiente (`temperatura` em °C) e dinâmica dos ventos (`vento_velocidade` em km/h e `vento_direcao` em graus).
- **Tipagem de Registros:** Distinção explícita entre dados reais de telemetria (`tipo_registro = OBSERVADO`) e previsões computacionais/IA (`tipo_registro = PREVISTO`).
- **Contratos DTO:**
  - `LeituraTelemetriaResponse`: Detalhamento técnico da medição/previsão pontual.
  - `TelemetriaDashboardResponse`: Agregação de status para o painel principal com indicadores de maré, chuva, vento e cota de risco.
  - `TelemetriaPontoGraficoDTO`: Estrutura de série temporal compatível diretamente com o componente de visualização gráfica (`TideChart.jsx`).

---

## 🔄 Fluxo de Onboarding em 2 Etapas (Integração Frontend)

O backend suporta nativamente o fluxo de cadastro dividido do frontend React:

```
Tela 1: /cadastro (Rápido)
  └── POST /auth/registro ──────────► Retorna Token JWT + estabelecimentoId
                                               │
                                               ▼
Tela 2: /estabelecimento (Completar)
  └── PUT /estabelecimento/{id} ────► Persiste Horários, Itens Críticos e Endereço
```

### 1. Etapa 1 — Cadastro Rápido (`/cadastro`)
- **Endpoint:** `POST /auth/registro` (Público)
- **Suporte a Aliases:** Aceita tanto chaves em inglês (`name`, `cnpj`, `neighborhood`) quanto em português (`nome`, `documento`, `bairro`).
- Cria o `Estabelecimento` com valores padrão provisórios e o `Usuario` com senha criptografada em BCrypt.
- **Retorno (201 Created):**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "estabelecimentoId": 1,
    "email": "contato@merceriaveropeso.com.br"
  }
  ```

### 2. Etapa 2 — Completar Perfil (`/estabelecimento`)
- **Endpoint:** `PUT /estabelecimento/{id}`
- **Headers:** `Authorization: Bearer <token>`
- **Payload compatível com o frontend:**
  ```json
  {
    "owner": "Maria Silva",
    "street": "Travessa Visconde de Souza Franco (Doca)",
    "number": "742",
    "type": "Mercado",
    "days": ["Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"],
    "opening": "07:30",
    "closing": "19:00",
    "criticalItems": ["Freezer no chão", "Estoque de secos", "Caixa registradora / CPU"]
  }
  ```
- Atualiza o endereço na tabela `estabelecimento`, sincroniza a grade na tabela `estabelecimento_horarios` e salva os bens na tabela `itens_criticos`.

---

## 🛰️ 1. Módulo Telemetria (`/telemetria`)

O módulo de **Telemetria** é responsável pela recolha, consolidação e estruturação dos dados ambientais em tempo real (temperatura, vento, chuva e nível do rio).

### Responsabilidades principais:
- Consumir dados meteorológicos e oceânicos do fornecedor externo (**Open-Meteo**).
- Agregar leituras de estações e sensores locais.
- Servir os dados consolidados para o dashboard do sistema e para outros módulos internos.

### DTO Principal: `TelemetriaDashboardResponse`
Estrutura de resposta anotada com Lombok, disponibilizando métodos de acesso aos sensores:
- `getTemperatura()`
- `getVentoVelocidade()`
- `getVentoDirecao()`
- `getChuvaAtual()`
- `getNivelAtual()`
- `getEstacaoCodigo()`

### Endpoints REST:
| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `GET` | `/telemetria/dashboard` | Retorna o estado atual dos indicadores ambientais consolidados. |

---

## 🤖 2. Módulo Risco (`/risco`) & Integração ML

O módulo de **Risco** orquestra o cálculo da probabilidade de alagamento na região metropolitana de Belém/PA. Ele combina os dados do `TelemetriaService` com o modelo preditivo de Machine Learning hospedado no serviço **FastAPI** (`rain-hourly-v1`).

### Fluxo de Execução do `RiscoService`:
1. **Recolha de Dados:** Invoca o `TelemetriaService` para obter o `TelemetriaDashboardResponse` atual, evitando chamadas redundantes a APIs externas.
2. **Mapeamento de Features:** Converte as métricas de telemetria no formato `snake_case` exigido pelo modelo de ML:
  - `temperature_2m`
  - `wind_speed_10m`
  - `precipitation`
  - `relative_humidity_2m`
  - `pressure_msl`
  - `cape`
3. **Invocação da API ML:** Envia os dados estruturados via cliente Feign/HTTP (`MlApiCliente`).
4. **Persistência:** Guarda a previsão retornada na entidade `PrevisaoChuvaMl` no banco de dados para históricos e auditoria de acurácia.
5. **Classificação do Risco:** Cruza a probabilidade calculada pelo modelo com o nível atual da maré em metros, definindo o nível de risco final (`BAIXO`, `MEDIO` ou `ALTO`).

### Endpoints REST:
| Método | Rota | Parâmetros | Descrição |
| :--- | :--- | :--- | :--- |
| `GET` | `/risco/atual` | `localidade` (opcional, default: `BELEM_CENTRO`) | Processa a telemetria e calcula o risco atual para a localidade. |

---

## 🔒 3. Integração, CORS e Segurança

- **Integração do Service:** O `RiscoService` comunica diretamente com o `TelemetriaService` via injeção de dependências do Spring.
- **CORS Global:** Gerido pelo bean `CorsConfig.java` e ativado na `SecurityFilterChain` (`SecurityConfig.java`) através de `.cors(Customizer.withDefaults())`.
- **OpenAPI / Swagger UI:** Módulos documentados com anotações `@Tag`, `@Operation` e `@ApiResponse` (dependência `springdoc-openapi-starter-webmvc-ui` v2.5.0).
- **Acesso à Documentação:** `/swagger-ui.html`, `/swagger-ui/**` e `/v3/api-docs/**` estão configurados como públicos no Spring Security.

## 📡 Resumo dos Endpoints da API

| Método | Rota | Descrição | Autenticação |
| :--- | :--- | :--- | :---: |
| `POST` | `/auth/registro` | Etapa 1: Criação de conta e estabelecimento inicial | Pública |
| `POST` | `/auth/login` | Autenticação de usuário e emissão de token JWT | Pública |
| `GET` | `/estabelecimento/me` | Consulta o perfil do estabelecimento vinculado ao usuário logado | Bearer Token |
| `GET` | `/estabelecimento/{id}` | Busca estabelecimento detalhado por ID | Bearer Token |
| `PUT` | `/estabelecimento/{id}` | Etapa 2: Atualização completa do perfil comercial | Bearer Token |
| `GET` | `/estabelecimento` | Lista todos os estabelecimentos ativos | Bearer Token |
| `GET` | `/alertas` | Lista alertas e comunicados da Defesa Civil | Pública |
| `POST` | `/ocorrencias` | Cadastro de sinistro com dados de perda e fotos | Bearer Token |

---

## 🚀 Execução Local

### Pré-requisitos
- **Java 21** instalado e configurado no `JAVA_HOME`.
- **Docker** e **Docker Compose** ativos.

### 1. Iniciar o Banco de Dados (PostgreSQL 16)
```bash
docker compose up -d
```
> O container `marealerta_pg` iniciará na porta `5432` com os bancos `marealerta_dev` e `marealerta_test`.

### 2. Executar a Aplicação
No Windows (PowerShell):
```powershell
./gradlew bootRun
```
No Linux / macOS:
```bash
./gradlew bootRun
```
> As migrações Flyway (`V1` a `V20`) são aplicadas automaticamente no boot.

### 3. Acessar a Documentação Interativa
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Painel Evolution API (WhatsApp):** [http://localhost:8081/manager](http://localhost:8081/manager)

---

## 🧪 Testes Automatizados

Para rodar a suíte completa de testes de integração (Spring Boot Test + Testcontainers/PostgreSQL):

```bash
./gradlew test
```

Os testes cobrem:
- Carregamento de contexto e validação estrita de esquemas Flyway (`ddl-auto: validate`).
- Ciclo de vida completo do onboarding em 2 etapas (`/auth/registro` ➔ `PUT /estabelecimento/{id}` ➔ `GET /estabelecimento/me`).
- Autenticação e bloqueio de credenciais inválidas.
- Proteção de rotas via `JwtAuthFilter`.

---

## 📄 Documentação Complementar

- **`docs/GUIA_BACKEND.md`**: Guia vivo da arquitetura, convenções, catálogo completo de migrações (V1 a V20), contratos de ML, operação da Evolution API e histórico contínuo de commits.
- **`docs/IA_PREVISAO_CHUVA_BACKEND.md`**: Especificação técnica das variáveis meteorológicas de Belém e contratos da pipeline de Machine Learning.
- **`backend-integracao.md`**: Guia de referência técnica do contrato entre frontend e backend, cobrindo o mapeamento de dados operacionais, cotas de alagamento e regras de persistência.
