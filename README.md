# Focus API

REST API for focus sessions: start, stop, and list finished sessions.

**Stack:** Kotlin, Spring Boot 4, WebFlux, JPA, PostgreSQL, Flyway.

This project uses a **local PostgreSQL** database for development. **Docker is not required** to run the app.

---

## Prerequisites

- Java 24 (see `build.gradle.kts` toolchain)
- PostgreSQL 16 (Homebrew on macOS is fine)

---

## Database setup (macOS / Homebrew)

Install and start PostgreSQL:

```bash
brew install postgresql@16
brew services start postgresql@16
```

Add the client to your PATH if `psql` is not found (Apple Silicon example):

```bash
echo 'export PATH="/opt/homebrew/opt/postgresql@16/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

Create the database and user (matches defaults in `application.yaml`):

```bash
createdb focus

psql postgres -c "CREATE USER focus WITH PASSWORD 'focus';"
psql postgres -c "GRANT ALL PRIVILEGES ON DATABASE focus TO focus;"
psql postgres -d focus -c "GRANT ALL ON SCHEMA public TO focus;"
```

Verify the connection:

```bash
psql "postgresql://focus:focus@localhost:5432/focus" -c "SELECT 1;"
```

Flyway runs migrations on startup (`src/main/resources/db/migration/`).

### Custom connection settings

Override defaults with environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/focus
export SPRING_DATASOURCE_USERNAME=focus
export SPRING_DATASOURCE_PASSWORD=focus
```

---

## Run the application

```bash
./gradlew bootRun
```

If you see `Connection to localhost:5432 refused`, PostgreSQL is not running or the database/user was not created.

---

## API documentation (Swagger)

With the app running:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

---

## Tests

```bash
./gradlew test
```

Integration tests use **Testcontainers** to start PostgreSQL in Docker during the test run. That is separate from local development: you can run the app with Homebrew Postgres only, but `./gradlew test` still needs Docker available for Testcontainers unless you change the test setup later.

---

## Main endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/focus/start` | Start a session |
| `POST` | `/focus/{id}/stop` | Stop a session |
| `GET` | `/focus/history/finished` | List stopped sessions (newest first) |
