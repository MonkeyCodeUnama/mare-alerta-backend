<div align="center">

# 🌊 Maré Alerta — Backend & Inteligência Preditiva

### Sistema Integrado de Monitoramento Hidrológico, Inferência de Risco por IA e Alertas Preventivos para a Bacia Hidrográfica de Belém/PA

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16%20Alpine-blue.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-ML%20Runtime-teal.svg?logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations%20V1--V24-red.svg?logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Docker](https://img.shields.io/badge/Docker%20Compose-Autocontido-2496ED.svg?logo=docker&logoColor=white)](https://www.docker.com/)
[![Tests](https://img.shields.io/badge/Tests-131%20Passing%20(100%25)-success.svg?logo=junit5&logoColor=white)]()

</div>

---

## 📌 1. Sobre o Projeto e seu Impacto Social

### O Desafio Hidrológico em Belém do Pará
A Região Metropolitana de Belém convive diariamente com uma das dinâmicas hidrológicas mais singulares do mundo: a confluência do **Rio Guamá** e da **Baía do Guajará** com o regime estuarino amazônico. Durante as **marés de sizígia** (alinhamento Sol-Terra-Lua), a cota das águas ultrapassa facilmente a marca crítica de **3,0 a 3,8 metros**, represando as saídas naturais dos canais urbanos. 

Quando essa preamar coincide com as **chuvas convectivas equatoriais torrenciais** — comuns na Amazônia no período da tarde —, o sistema de drenagem entra em colapso. Bacias comerciais históricas e vias vitais de tráfego (como **Ver-o-Peso, Doca de Souza Franco, Reduto, Campina, Cidade Velha, Tamandaré e Estrada Nova**) sofrem alagamentos abruptos em questão de minutos.

```
       [ Maré Astronômica de Sizígia ]       +       [ Chuva Convectiva Equatorial ]
          (Cota Guajará > 3.0m)                          (Precipitação Intensa)
                                      \             /
                                       \           /
                                  [ TRANSBORDAMENTO ]
                           Bloqueio de canais e refluxo urbano
                                        |
                 Prejuízos a Comércios | Perda de Estoque | Risco Humano
```

### O Valor Social e Econômico do Maré Alerta
O impacto desses alagamentos recai de forma desproporcional sobre **pequenos e médios comerciantes locais** (padarias, mercearias, peixarias, açougues, farmácias e feirantes), além de moradores e transeuntes:
- **Perdas Financeiras Devastadoras:** Perda de motores de refrigeração, freezers queimados, estoques no nível do solo destruídos pela água fluvial e dias inteiros com as portas fechadas.
- **Insegurança e Falta de Previsibilidade:** As tábuas de maré convencionais não cruzam dados astronômicos com previsão de chuva horária, deixando o comerciante sem tempo hábil de reação.
- **Risco à Saúde Pública:** A água de refluxo dos canais carrega contaminação, expondo trabalhadores e clientes a doenças de veiculação hídrica.

### A Missão da Plataforma
O **Maré Alerta** preenche essa lacuna através de tecnologia pública e aberta:
1. **Telemetria 100% Real:** Coleta contínua da cota de maré (Open-Meteo Marine) e de 20 variáveis atmosféricas ao vivo, sem valores fictícios ou simulados.
2. **Inteligência Artificial Preditiva:** Microserviço dedicado que processa o modelo `rain-hourly-v1` para prever chuva com antecedência horária precisa.
3. **Alertas Diretos via WhatsApp:** Envio automatizado e gratuito de mensagens para os comerciantes da área sob risco, com tempo hábil para elevação de itens.
4. **Prevenção Ativa com Checklists:** Ferramentas digitais para que o comerciante saiba exatamente o que fazer antes da água subir (levantar freezers, desligar disjuntores, salvar estoque).
5. **Auditoria e Apoio à Defesa Civil:** Registro oficial de sinistros com fotos e relatórios estruturados para fundamentar planos de contingência pública.

---

## 🛠️ 2. Stack Tecnológica

O backend do **Maré Alerta** foi construído seguindo rigorosos padrões de engenharia de software corporativa, arquitetura em camadas (Controller-Service-Repository), persistência estrita na 3ª Forma Normal e desacoplamento de serviços via microsserviços e contêineres:

| Camada | Tecnologia | Versão | Função / Responsabilidade |
| :--- | :--- | :--- | :--- |
| **Backend Principal** | **Java** | **21 (LTS)** | Linguagem base de alto desempenho e tipagem estrita |
| **Framework Web** | **Spring Boot** | **4.1.1** | Núcleo do backend, injeção de dependências e APIs RESTful |
| **Gerenciador de Build** | **Gradle** | **9.7.1** | Build automatizado com Kotlin DSL (`build.gradle.kts`) |
| **Banco de Dados** | **PostgreSQL** | **16 Alpine** | Persistência relacional em 3FN com constraints e integridade estrita |
| **Versionamento de Banco**| **Flyway** | **Migrations V1 a V24** | Evolução controlada e idempotente do schema de banco |
| **Inteligência Artificial** | **FastAPI / Python** | **3.13-slim** | Microserviço de ML desacoplado (`weather-model`) rodando o modelo SGD |
| **Bibliotecas de ML** | **Scikit-learn & Joblib** | Latest | Pipeline de pré-processamento e inferência do modelo `rain-hourly-v1` |
| **Gateway WhatsApp** | **Evolution API v2** | **v2.2.3** | Gateway WhatsApp baseado em Baileys/WebSockets e API RESTful |
| **Telemetria Marinha** | **Open-Meteo Marine** | API Pública | Cota horária real do nível do mar (`sea_level_height_msl`) em Belém |
| **Previsão Climática** | **Open-Meteo GFS Global** | API Pública | 20 variáveis atmosféricas superficiais e de altitude em tempo real |
| **Segurança & Tokens** | **Spring Security + JWT** | **JJWT 0.12.6** | Autenticação stateless, Access Token e Refresh Token seguro |
| **Proteção de Tráfego** | **Bucket4j + Caffeine** | **8.10.1 / 3.1.8** | Rate Limiting em memória por IP (Token Bucket) contra força bruta |
| **Documentação da API** | **Springdoc OpenAPI 3** | **3.1.1** | Interface interativa Swagger UI e especificação OpenAPI JSON |
| **Orquestração** | **Docker & Compose** | **v2** | Orquestração dos 4 serviços com healthchecks e build autocontido |

---

## 🚀 3. Guia Detalhado de Execução

Você pode executar o projeto de duas formas:
- **Opção A (Recomendada):** 100% via **Docker Compose**, subindo toda a aplicação (Postgres, IA, WhatsApp e Backend) com um único comando e sem necessidade de instalar Java ou Python na máquina.
- **Opção B (Desenvolvimento Ativo):** Executando os serviços de infraestrutura no Docker e a aplicação Spring Boot localmente no host via Gradle para depuração em tempo real.

---

### Pré-requisitos
Antes de começar, certifique-se de ter instalado no seu computador:
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (com Docker Compose v2 ativo).
- [Git](https://git-scm.com/) instalado.
- *(Opcional, apenas para Opção B)* [JDK 21](https://adoptium.net/) configurado no `PATH`.

---

### Passo 1: Clonar o Repositório

Abra o seu terminal (PowerShell no Windows, ou Bash/Zsh no Linux/macOS) e execute:

```bash
git clone https://github.com/MonkeyCodeUnama/mare-alerta-backend.git
cd mare-alerta-backend
```

---

### Passo 2: Configurar o Arquivo de Ambiente (`.env`)

O projeto utiliza um arquivo `.env` para centralizar credenciais de banco, segredos JWT e portas de serviço. Um modelo pronto e funcional de desenvolvimento já está disponível:

```bash
# No Linux ou macOS:
cp .env.example .env

# No Windows (PowerShell):
Copy-Item .env.example .env
```

> **Nota:** As configurações padrão do `.env.example` já vêm calibradas para desenvolvimento local (`POSTGRES_DB=marealerta_dev`, portas `8080`, `8000`, `8081` e `5432`). Nenhuma alteração manual é obrigatória para a execução inicial.

---

### Passo 3: Executar via Docker Compose (Modo 100% Autocontido)

Esta é a opção padrão e recomendada. O Docker Compose iniciará os 4 contêineres na ordem correta, aguardando os testes de saúde (*healthchecks*):

```bash
docker compose up -d --build
```

#### O que o Docker Compose inicializa:
1. **`marealerta_pg` (Porta `5432`):** Sobe o PostgreSQL 16 Alpine e aguarda estar pronto para receber conexões (`pg_isready`).
2. **`marealerta_ia` (Porta `8000`):** Compila o microserviço Python FastAPI a partir da pasta autocontida `docker/weather-model/`, carrega o modelo treinado `model_bundle.joblib` e testa a rota de saúde `/health`.
3. **`marealerta_whatsapp` (Porta `8081`):** Inicializa a Evolution API v2, criando automaticamente o schema isolado `evolution` no PostgreSQL.
4. **`marealerta_app` (Porta `8080`):** Aguarda o PostgreSQL e a IA estarem saudáveis (`condition: service_healthy`), executa as 24 migrações do Flyway e inicia a API Spring Boot.

#### Verificando o Status de Saúde:
Para checar se todos os serviços subiram corretamente:

```bash
docker compose ps
```

Todos os serviços devem apresentar status `Up` ou `healthy`:

```text
NAME                  IMAGE                        COMMAND                  SERVICE             STATUS
marealerta_app        mare-alerta-backend-app      "sh -c 'sleep 3 && j…"   app                 Up (healthy)
marealerta_ia         mare-alerta-backend-weather  "uvicorn app:app --h…"   weather-model       Up (healthy)
marealerta_pg         postgres:16-alpine           "docker-entrypoint.s…"   postgres            Up (healthy)
marealerta_whatsapp   evoapicloud/evolution-api    "docker-entrypoint.s…"   evolution_api       Up
```

#### Acompanhando os Logs da Aplicação:
Para visualizar a inicialização e os logs em tempo real:

```bash
# Ver logs do backend Spring Boot:
docker compose logs -f app

# Ver logs do modelo preditivo de IA:
docker compose logs -f weather-model
```

#### Parando os Serviços:
Para encerrar a execução dos contêineres mantendo os dados persistidos:

```bash
docker compose down
```

---

### Passo 4: Execução Híbrida (Infraestrutura no Docker + Backend no Host)

Se você estiver desenvolvendo no código Java e deseja reinicializações instantâneas via hot-reload ou depuração na IDE (IntelliJ, VS Code, Eclipse):

1. **Suba apenas as dependências de infraestrutura no Docker:**
   ```bash
   docker compose up -d postgres weather-model evolution_api
   ```

2. **Execute o backend via Gradle:**
   - **No Windows (PowerShell):**
     ```powershell
     ./gradlew bootRun
     ```
   - **No Linux ou macOS:**
     ```bash
     ./gradlew bootRun
     ```

As migrações do banco (`V1` a `V24`) serão aplicadas automaticamente pelo Flyway na inicialização.

---

### Passo 5: Conectar o WhatsApp na Evolution API (Opcional para Alertas)

Caso deseje testar os disparos de alertas de enchente via WhatsApp para comerciantes:

1. Abra no navegador o painel administrativo da Evolution API: **`http://localhost:8081/manager`**.
2. No campo de autenticação, informe a API Key padrão: `marealerta_secret_key_2026`.
3. Localize a instância **`marealerta_belem`** e clique no botão do **QR Code**.
4. No seu WhatsApp do celular: vá em *Configurações* (ou menu de 3 pontinhos) ➡️ *Aparelhos Conectados* ➡️ *Conectar um aparelho* e aponte a câmera para a tela.
5. Uma vez conectado, o status mudará para `open` e o backend poderá despachar mensagens automaticamente.

---

### Passo 6: Executar a Bateria de Testes Automatizados

O backend conta com uma suíte de **131 testes automatizados** (testes unitários, mocks de terceiros, validações de controllers e integridade de repositórios). Para executá-la:

```bash
# No Windows (PowerShell):
./gradlew test --no-daemon

# No Linux ou macOS:
./gradlew test
```

O relatório visual completo em HTML é gerado em:
`build/reports/tests/test/index.html`

---

## 🌐 4. Portas e URLs de Acesso

Com o ambiente em execução, acesse os seguintes serviços e documentações:

| Serviço | URL de Acesso | Descrição |
| :--- | :--- | :--- |
| **Documentação Interativa (Swagger UI)** | [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html) | Teste e visualize todos os endpoints da API pelo navegador |
| **Esquema OpenAPI (JSON)** | [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs) | Contrato formal consumido pelo frontend e geradores de SDK |
| **Imagens de Sinistros (Uploads)** | `http://localhost:8080/uploads/ocorrencias/{arquivo}` | Servidor de fotos e comprovantes anexados pelo usuário |
| **Microserviço IA (FastAPI Docs)** | [`http://localhost:8000/docs`](http://localhost:8000/docs) | Swagger interativo do modelo preditivo de Machine Learning |
| **Healthcheck da IA** | [`http://localhost:8000/health`](http://localhost:8000/health) | Verificação de integridade e carregamento do modelo joblib |
| **Painel WhatsApp (Evolution API)** | [`http://localhost:8081/manager`](http://localhost:8081/manager) | Painel web para gestão de instâncias e conexão QR Code |
| **Banco de Dados (PostgreSQL)** | `localhost:5432` | Banco relacional (Database: `marealerta_dev`, User: `postgres`) |

---

## ⚡ 5. Exemplos Rápidos de Uso da API

Você pode testar as principais rotas da API diretamente pelo Swagger UI ou utilizando comandos `curl` ou PowerShell:

### 1. Consultar Dashboard de Telemetria e Maré em Tempo Real
Retorna a cota atual do mar em Belém (MSL), a tendência (`SUBINDO`, `DESCENDO`), o horário previsto do preamar e o nível de risco:
```bash
curl -X GET http://localhost:8080/telemetria/dashboard
```

### 2. Avaliar Previsão de Risco com Inferência da IA
Orquestra a coleta ao vivo de 20 parâmetros atmosféricos e executa o modelo preditivo:
```bash
curl -X GET "http://localhost:8080/risco/atual?localidade=Campina"
```

### 3. Autenticar Usuário e Obter Token JWT
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"comerciante@belem.com","senha":"senhaSegura123"}'
```

---

## 📁 6. Estrutura de Diretórios do Projeto

```text
mare-alerta-backend/
├── docker/
│   └── weather-model/                 # Runtime autocontido do modelo preditivo de IA
│       ├── Dockerfile                 # Imagem Python 3.13-slim otimizada
│       ├── requirements.txt           # Dependências (FastAPI, scikit-learn, joblib)
│       ├── app.py                     # API FastAPI (/health, /v1/predict)
│       ├── rain_runtime.py            # Pipeline de inferência SGD calibrado
│       └── artifacts/                 # model_bundle.joblib e metadata.json
├── docs/                              # Documentação técnica e de arquitetura
│   ├── GUIA_BACKEND.md                # Guia vivo de desenvolvimento e histórico de commits
│   └── IA_PREVISAO_CHUVA_BACKEND.md   # Especificação formal das features do modelo de ML
├── src/
│   ├── main/
│   │   ├── java/com/marealerta/backend/
│   │   │   ├── alerta/                # Emissão e despacho de comunicados de emergência
│   │   │   ├── auth/                  # Autenticação JWT, login, registro e refresh token
│   │   │   ├── checklist/             # Checklists de prevenção e mitigação de danos
│   │   │   ├── estabelecimento/       # Cadastro de comércios, endereçamento e horários
│   │   │   ├── itens/                 # Cadastro e proteção de bens em cota crítica
│   │   │   ├── ocorrencias/           # Registro de sinistros, protocolos e upload de fotos
│   │   │   ├── risco/                 # Integração com IA, orquestração e matriz de risco
│   │   │   ├── shared/                # Filtros de Rate Limit, CORS, Security e WebConfig
│   │   │   ├── telemetria/            # Ingestão de dados Open-Meteo e cota de maré
│   │   │   └── usuario/               # Entidade de credenciais e vínculos
│   │   └── resources/
│   │       ├── db/migration/          # 24 migrações Flyway (V1 a V24)
│   │       ├── application.yaml       # Configuração base do Spring Boot
│   │       └── application-prod.yaml  # Configuração de produção
│   └── test/                          # 131 testes automatizados (unitários e controllers)
├── docker-compose.yml                 # Orquestrador dos 4 contêineres do ecossistema
├── Dockerfile                         # Build multi-stage da aplicação Java 21
├── build.gradle.kts                   # Configuração de build Gradle Kotlin DSL
├── .env.example                       # Gabarito de variáveis de ambiente
└── README.md                          # Este documento oficial
```

---

## 👥 7. Autoria e Desenvolvimento

Projeto concebido e desenvolvido pela equipe de engenharia do **Maré Alerta** no âmbito da **Unama / MonkeyCode**:
- **Arquitetura de Software & Backend:** Daniel das Neves (`AKIRA-NZR`)
- **Inteligência Artificial & Meteorologia:** Equipe de Machine Learning Maré Alerta
- **Interface & Experiência:** Equipe Frontend & Mobile Maré Alerta
