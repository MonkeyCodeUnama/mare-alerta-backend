<div align="center">

# 🌊 Maré Alerta — Backend

### Plataforma Inteligente de Telemetria Hidrometeorológica, Predição por IA e Alertas Preventivos de Alagamento para Belém/PA

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16%20Alpine-blue.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-ML%20Runtime-teal.svg?logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![Docker](https://img.shields.io/badge/Docker%20Compose-Autocontido-2496ED.svg?logo=docker&logoColor=white)](https://www.docker.com/)
[![Tests](https://img.shields.io/badge/Tests-131%20Passing%20(100%25)-success.svg?logo=junit5&logoColor=white)]()

</div>

---

## 📌 1. Sobre o Projeto e Valor Social

Em Belém/PA, o encontro do Rio Guamá e da Baía do Guajará gera **marés de sizígia superiores a 3,0 metros**. Quando essas marés coincidem com as **chuvas equatoriais intensas**, os canais urbanos represam e vias comerciais críticas (Ver-o-Peso, Doca, Campina, Reduto, Jurunas) alagam em poucos minutos.

O principal impacto atinge **pequenos comerciantes locais** e moradores: perda recorrente de motores, freezers, estoques no solo, dias inteiros de portas fechadas e riscos sanitários severos.

O **Maré Alerta** resolve esse problema transformando dados em prevenção ativa:
- **Telemetria 100% Real:** Coleta contínua da cota de maré (Open-Meteo Marine) e 20 variáveis atmosféricas ao vivo.
- **Predição por IA:** Microserviço dedicado com modelo calibrado (`rain-hourly-v1`) para prever chuva com 1 hora de antecedência.
- **Alertas Gratuitos no WhatsApp:** Disparo automatizado por bairro antes do pico da maré (*preamar*).
- **Checklist Preventivo:** Ações práticas para o comerciante salvar seus bens antes da água subir.
- **Apoio à Defesa Civil:** Registro oficial de ocorrências com fotos e histórico auditável de sinistros.

---

## 🛠️ 2. Stack Tecnológica

| Camada | Tecnologia / Ferramenta | Descrição |
| :--- | :--- | :--- |
| **Backend** | Java 21 LTS + Spring Boot 4.1.1 | API RESTful, injeção de dependências e regras de negócio |
| **Build & Testes** | Gradle 9.7 (Kotlin DSL) + JUnit 5 | 131 testes automatizados (unitários, mocks e controllers) |
| **Banco de Dados** | PostgreSQL 16 + Flyway | 24 migrações ativas, modelagem 3FN e constraints estritas |
| **Inteligência Artificial** | Python 3.13 + FastAPI + Scikit-learn | API preditiva separada (`mare-alerta-ia`) rodando modelo SGD |
| **Telemetria & Clima** | Open-Meteo (Marine & GFS Global) | Nível do mar (`sea_level_height_msl`) e 20 features atmosféricas em UTC |
| **Mensageria** | Evolution API v2 (WhatsApp Baileys) | Gateway de mensagens com suporte a QR Code e multi-instâncias |
| **Segurança** | Spring Security + JWT + Bucket4j | Tokens Stateless (Access + Refresh Token) e Rate Limit por IP |
| **Infraestrutura** | Docker & Docker Compose | Orquestração autocontida com healthchecks nativos |

---

## 🚀 3. Como Executar o Projeto

### Pré-requisitos
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (com Docker Compose v2 ativo).
- [Git](https://git-scm.com/).
- *(Opcional, apenas para rodar local sem Docker)* [JDK 21](https://adoptium.net/).

---

### Passo 1: Clonar o Repositório
```bash
git clone https://github.com/MonkeyCodeUnama/mare-alerta-backend.git
cd mare-alerta-backend
```

---

### Passo 2: Configurar o Arquivo de Ambiente
Copie o gabarito oficial com as configurações de desenvolvimento:

```bash
# Linux / macOS:
cp .env.example .env

# Windows (PowerShell):
Copy-Item .env.example .env
```
> As credenciais padrão do `.env.example` já são 100% funcionais para ambiente local.

---

### Passo 3: Executar a Aplicação

#### Opção A — 100% Docker Compose (Recomendado)
Sobe todo o ecossistema (Postgres, IA, WhatsApp e Backend) com um único comando:

```bash
docker compose up -d --build
```

- **Verificar saúde dos contêineres:**
  ```bash
  docker compose ps
  ```
  *Todos os 4 contêineres (`marealerta_app`, `marealerta_ia`, `marealerta_pg`, `marealerta_whatsapp`) devem indicar status `Up` ou `healthy`.*

- **Acompanhar os logs:**
  ```bash
  docker compose logs -f app
  ```

- **Parar os serviços:**
  ```bash
  docker compose down
  ```

---

#### Opção B — Execução Híbrida (Desenvolvimento Ativo)
Ideal para programar e debugar o código Java com reinicialização rápida:

1. **Inicie a infraestrutura de apoio:**
   ```bash
   docker compose up -d postgres evolution_api
   ```

   > Defina `IA_CHUVA_URL` para a API separada da IA (`mare-alerta-ia`), por exemplo:
   > `IA_CHUVA_URL=https://ia.seu-dominio.com/v1/predict`

2. **Execute o backend via Gradle:**
   ```bash
   # Windows (PowerShell):
   ./gradlew bootRun

   # Linux / macOS:
   ./gradlew bootRun
   ```

---

### Passo 4: Conectar o WhatsApp (Opcional para Alertas)
1. Acesse o painel web: [`http://localhost:8081/manager`](http://localhost:8081/manager).
2. Autentique com a chave: `marealerta_secret_key_2026`.
3. Na instância `marealerta_belem`, abra o **QR Code** e escaneie pelo aplicativo do WhatsApp (*Aparelhos Conectados*).

---

### Passo 5: Executar os Testes Automatizados
```bash
# Windows (PowerShell):
./gradlew test --no-daemon

# Linux / macOS:
./gradlew test
```
*Relatório visual gerado em: `build/reports/tests/test/index.html`.*

---

## 🌐 4. URLs de Acesso e Endpoints

| Serviço | URL | Descrição |
| :--- | :--- | :--- |
| **Swagger UI (Backend)** | [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html) | Documentação interativa para teste de todos os endpoints |
| **OpenAPI Schema (JSON)** | [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs) | Especificação OpenAPI 3 da aplicação |
| **Fotos de Ocorrências** | `http://localhost:8080/uploads/ocorrencias/{arquivo}` | Servidor estático das fotos de sinistro |
| **FastAPI Docs (IA)** | [`http://localhost:8000/docs`](http://localhost:8000/docs) | Swagger do modelo preditivo de Machine Learning |
| **Healthcheck da IA** | [`http://localhost:8000/health`](http://localhost:8000/health) | Verificação de integridade do modelo de ML |
| **Painel WhatsApp** | [`http://localhost:8081/manager`](http://localhost:8081/manager) | Painel da Evolution API para gestão de instâncias |
| **PostgreSQL** | `localhost:5432` | Banco de dados `marealerta_dev` (usuário: `postgres`) |

---

### ⚡ Exemplos Rápidos de Chamadas

```bash
# 1. Consultar cota da maré e nível de risco atual:
curl -X GET http://localhost:8080/telemetria/dashboard

# 2. Avaliar risco de alagamento por IA para um bairro:
curl -X GET "http://localhost:8080/risco/atual?localidade=Campina"

# 3. Autenticação (Login):
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"comerciante@belem.com","senha":"senhaSegura123"}'
```

---

## 📁 5. Estrutura do Repositório

```text
mare-alerta-backend/
├── docker/
├── docs/
│   ├── GUIA_BACKEND.md       # Guia vivo de arquitetura e histórico de atualizações
│   └── IA_PREVISAO_CHUVA...  # Especificação técnica do modelo meteorológico
├── src/
│   ├── main/java/com/marealerta/backend/
│   │   ├── alerta/           # Emissão e despacho de comunicados de emergência
│   │   ├── auth/             # Autenticação JWT, login, registro e refresh token
│   │   ├── checklist/        # Checklists preventivos de mitigação de danos
│   │   ├── estabelecimento/  # Perfil comercial, endereçamento e horários
│   │   ├── itens/            # Bens em cota crítica vulneráveis a alagamento
│   │   ├── ocorrencias/      # Registro de sinistros, protocolos e upload de fotos
│   │   ├── risco/            # Integração com o microserviço de IA e matriz de risco
│   │   ├── shared/           # Configurações de CORS, Rate Limit e WebConfig
│   │   ├── telemetria/       # Ingestão de dados Open-Meteo e cota de maré
│   │   └── usuario/          # Entidade de credenciais e segurança
│   └── test/                 # 131 testes automatizados (unitários e controllers)
├── docker-compose.yml        # Orquestrador dos 4 contêineres com healthchecks
├── Dockerfile                # Build multi-stage da aplicação Java 21
├── build.gradle.kts          # Script de build Gradle Kotlin DSL
├── .env.example              # Gabarito de variáveis de ambiente
└── README.md                 # Documentação principal
```

---

<div align="center">
Desenvolvido pela equipe de engenharia do <strong>Maré Alerta</strong> na <strong>Unama / MonkeyCode</strong>.
</div>
