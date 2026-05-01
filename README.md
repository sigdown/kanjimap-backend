# Ktor REST Template

A small Ktor REST API starter with PostgreSQL, HikariCP, Flyway migrations, and Exposed DSL.

## Stack

- Kotlin JVM
- Ktor Server with Netty
- kotlinx.serialization JSON
- PostgreSQL JDBC driver
- HikariCP datasource
- Flyway SQL migrations
- JetBrains Exposed DSL over JDBC

## Run locally

Start PostgreSQL:

```bash
docker compose up -d
```

Run the API with a locally installed Gradle:

```bash
gradle run
```

Optional: generate a Gradle wrapper after opening the project:

```bash
gradle wrapper
./gradlew run
```

Health check:

```bash
curl http://localhost:8080/health
```

Create a user:

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"vasily@example.com","name":"Vasily"}'
```

List users:

```bash
curl http://localhost:8080/api/v1/users
```

## Environment variables

The app reads env vars first, then falls back to `src/main/resources/application.yaml`.

```bash
PORT=8080
DATABASE_JDBC_URL=jdbc:postgresql://localhost:5432/app
DATABASE_USER=app
DATABASE_PASSWORD=app
DATABASE_MAX_POOL_SIZE=10
```

## Migration flow

SQL migrations live in:

```text
src/main/resources/db/migration
```

Flyway runs automatically during app startup before Exposed connects. Add new migrations as:

```text
V2__your_change.sql
V3__another_change.sql
```

## REST endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/health` | Health check |
| GET | `/api/v1/users` | List users |
| GET | `/api/v1/users/{id}` | Get user by ID |
| POST | `/api/v1/users` | Create user |
| PUT | `/api/v1/users/{id}` | Update user |
| DELETE | `/api/v1/users/{id}` | Delete user |

## Project structure

```text
src/main/kotlin/com/example
  Application.kt
  config/AppConfig.kt
  database/DataSourceFactory.kt
  database/DatabaseFactory.kt
  plugins/
  users/
src/main/resources
  application.yaml
  logback.xml
  db/migration/V1__create_users.sql
```

## Notes

This template uses Exposed DSL, not DAO. That keeps the database layer explicit and avoids magic entity state in a REST API, because apparently production systems enjoy clarity.
