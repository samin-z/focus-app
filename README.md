# Focus API

REST API for focus sessions: start, stop, and list sessions.

**Stack:** Kotlin, Spring Boot 4, WebFlux, JPA, PostgreSQL, Flyway.

This project uses **PostgreSQL** for development. Pick one setup below.

---

## Prerequisites

- Java 24 (see `build.gradle.kts` toolchain)
- PostgreSQL — via **Docker Compose** or **Homebrew** (pick one)

---

## Database setup

### Option A — Docker Compose (recommended if you use Docker)

Start Postgres:

```bash
docker compose up -d
```

Or run the app and let Spring Boot start the container for you:

```bash
SPRING_PROFILES_ACTIVE=docker ./gradlew bootRun
```

(`spring-boot-docker-compose` is enabled only with the `docker` profile.)

Defaults match `application.yaml`: database `focus`, user `focus`, password `focus`, port `5432`.

Stop the database:

```bash
docker compose down
```

Remove data as well:

```bash
docker compose down -v
```

**Port conflict:** if Homebrew Postgres is already on `5432`, stop it (`brew services stop postgresql@16`) or only use one setup.

### Option B — Homebrew Postgres (no Docker for the app)

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

Wait until the log shows **`Started FocusApplication`** — only then is the server listening on port 8080.

If you see `Connection to localhost:5432 refused`, Postgres is not running (start Docker Compose or Homebrew Postgres).

### Swagger not loading?

`BUILD SUCCESSFUL` from Gradle does **not** always mean the app is still running. Check the log for errors above that line.

Common fix if you see `No host port mapping found for container port 5432`:

- A leftover Docker Postgres container (often from tests) conflicts with Compose integration.
- **Homebrew Postgres (default):** use plain `./gradlew bootRun` (Compose integration is off by default).
- **Docker Compose:** run `docker compose down`, then `docker compose up -d`, then `SPRING_PROFILES_ACTIVE=docker ./gradlew bootRun`.

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
| `GET` | `/focus/history/finished` | List active and stopped sessions (newest activity first) |
