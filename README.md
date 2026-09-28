# Maré Alerta — Backend

API RESTful do ecossistema **Maré Alerta**, desenvolvida em **Java 21** e **Spring Boot**. O sistema provê inteligência preditiva, monitoramento hidrológico e proteção patrimonial para comerciantes localizados em áreas de cota crítica da bacia hidrográfica de Belém/PA.

---

## 🛠️ Stack Tecnológica

- **Linguagem & Framework:** Java 21 (LTS) & Spring Boot 4.x
- **Persistência & ORM:** Spring Data JPA / Hibernate com PostgreSQL 16
- **Versionamento de Banco:** Flyway Migrations (V1 a V15)
- **Segurança:** Spring Security, JWT (HMAC-SHA256) e criptografia de senhas com BCrypt
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
- **`estabelecimento` (V1, V11):** Perfil comercial com endereço estruturado (`logradouro`, `numero`, `bairro`, `cep`, `municipio`, `uf`), tipo de negócio e soft delete (`deleted_at`).
- **`bairro` (Enum oficial):** Catálogo com os bairros oficiais de Belém (Campina, Umarizal, Marco, Cidade Velha, Jurunas, etc.), com índice B-Tree e tolerância a acentos.
- **`estabelecimento_horarios` (V3):** Grade de funcionamento semanal normalizada por dia da semana (`dia_semana` 1..7) e horários (`TIME`).
- **`itens_criticos` (V7):** Relação de bens vulneráveis à cota de alagamento (freezers, motores, estoque no chão, computadores).
- **`itens_checklist` (V12):** Itens de verificação e checklist preventivo de maré alta com prioridades e status.
- **`ocorrencias` & `ocorrencia_fotos` (V13, V14):** Registro oficial de sinistros pós-alagamento com protocolo estável, dados comerciais e anexos de fotos com hash SHA-256.
- **`leituras_telemetria` (V15):** Histórico de cotas observadas, níveis de maré e previsões hidrológicas.
- **`alertas` (V4):** Comunicações de emergência e cotas de alerta emitidas pela Defesa Civil.

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
> As migrações Flyway (`V1` a `V15`) são aplicadas automaticamente no boot.

### 3. Acessar a Documentação Interativa
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

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

- [REFERENCIA_INTEGRACAO.md](file:///c:/Users/dkaua/Documents/programação/Maré%20alerta-backend/mare-alerta-backend/REFERENCIA_INTEGRACAO.md): Guia detalhado do contrato frontend-backend e mapeamento de dados operacionais.
