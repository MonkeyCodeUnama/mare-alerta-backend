# Maré Alerta — Backend

Backend Java/Spring Boot do Maré Alerta. Nesta etapa, a API oferece apenas autenticação por JWT.

## Requisitos

- Java 21
- Docker e Docker Compose, para PostgreSQL local
- Uma chave JWT Base64 que decodifique para pelo menos 32 bytes

## Execução local

1. Inicie o banco: `docker compose up -d`.
2. Exporte uma chave JWT: `export JWT_KEY='MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE='`.
3. Execute: `./gradlew bootRun`.

O perfil `dev` conecta em `jdbc:postgresql://localhost:5432/marealerta_dev`. As migrations Flyway são aplicadas automaticamente.

## Executar com Docker

Construa a imagem e inicie a API junto com o PostgreSQL:

```bash
docker compose up --build
```

A API fica disponível em `http://localhost:8080` e o Swagger UI em `http://localhost:8080/swagger-ui/index.html`. Para definir uma chave JWT própria, exporte `JWT_KEY` antes de iniciar o Compose. Encerre com `docker compose down`; para remover também os dados locais do banco, use `docker compose down -v`.

Para configuração de produção, defina `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_KEY` e, opcionalmente, `JWT_EXPIRATION`.

## API de autenticação

### `POST /auth/registro`

Cria um usuário e seu estabelecimento, retornando `201 Created` e `{ "token": "..." }`.

```json
{
  "email": "usuario@example.com",
  "senha": "senha-com-8-ou-mais-caracteres",
  "estabelecimento": {
    "nome": "Mercado Central",
    "documento": "12345678901",
    "tipoNegocio": "MERCADO",
    "endereco": "Rua das Flores, 10",
    "latitude": -23.550520,
    "longitude": -46.633308
  }
}
```

E-mail já existente retorna `409 Conflict`; campos inválidos retornam `400 Bad Request`.

### `POST /auth/login`

Recebe `{ "email": "...", "senha": "..." }` e retorna `200 OK` com `{ "token": "..." }`. Credenciais inválidas retornam `401 Unauthorized`.

Com a aplicação em execução, a documentação interativa está em `/swagger-ui/index.html` e o contrato OpenAPI em `/v3/api-docs`.

## Testes e CI

Execute `SPRING_PROFILES_ACTIVE=test JWT_KEY='MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=' ./gradlew test` com PostgreSQL disponível em `localhost:5432`.

O workflow GitHub Actions em `.github/workflows/ci.yml` executa automaticamente testes e migrations em PostgreSQL 16 para pushes e pull requests destinados a `main`.
