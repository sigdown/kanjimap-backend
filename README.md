# Kanjimap Backend

Backend for a Japanese learning app. The project exposes a REST API for:

- authentication,
- dictionary search for words and kanji,
- learning blocks,
- per-user progress,
- review/check flows.

The project was built with Ktor, PostgreSQL, Flyway, Exposed DSL, and JWT authentication.

## Stack

- Kotlin JVM 17
- Ktor Server 3
- PostgreSQL
- Flyway
- Exposed DSL
- HikariCP
- kotlinx.serialization
- JWT auth
- JUnit 5 + Testcontainers

## API documentation

OpenAPI specification is stored in the project root:

```text
openapi.yaml
```

You can open it in Swagger Editor, IntelliJ HTTP client plugins, or any OpenAPI-compatible viewer.

## Run locally

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Configure environment

The app reads real environment variables first and then falls back to values from a local `.env` file.

Minimum required variables:

```bash
SERVER_HOST=0.0.0.0
SERVER_PORT=8080

DATABASE_JDBC_URL=jdbc:postgresql://localhost:5432/app
DATABASE_USER=app
DATABASE_PASSWORD=app
DATABASE_MAX_POOL_SIZE=10

JWT_SECRET=dev-jwt-secret-change-me
JWT_ISSUER=kanjimap-backend
JWT_AUDIENCE=kanjimap-clients
JWT_REALM=kanjimap-api
JWT_EXPIRATION_MS=86400000
```

### 3. Start the server

Windows:

```bash
gradlew.bat run
```

Linux/macOS:

```bash
./gradlew run
```

After startup the API is available at:

```text
http://localhost:8080
```

Health check:

```bash
curl http://localhost:8080/health
```

## Main endpoints

### Public

- `GET /health`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/words?query=...`
- `GET /api/words/{id}`
- `GET /api/kanji?query=...`
- `GET /api/kanji/{id}`
- `GET /api/learning/blocks`
- `GET /api/learning/blocks/{id}`

### JWT protected

Use header:

```text
Authorization: Bearer <accessToken>
```

Protected endpoints:

- `GET /api/auth/me`
- `GET /api/progress/words/{id}`
- `PUT /api/progress/words/{id}`
- `GET /api/progress/kanji/{id}`
- `PUT /api/progress/kanji/{id}`
- `GET /api/review/words`
- `GET /api/review/kanji`
- `POST /api/review/words/{id}/check`
- `POST /api/review/kanji/{id}/check`

## Database

Migrations are stored in:

```text
src/main/resources/db/migration
```

Flyway runs automatically on application startup before the database is used by Exposed.

## Tests

Run all tests:

Windows:

```bash
gradlew.bat test
```

Linux/macOS:

```bash
./gradlew test
```

Notes:

- unit tests run without Docker,
- repository integration tests use Testcontainers,
- Docker Desktop or another working Docker environment is required for the full test suite.

## Project structure

```text
src/main/kotlin/com/example
  app/
  application/
  domain/
  infra/
  presentation/
src/main/resources
  db/migration/
openapi.yaml
docker-compose.yml
```
