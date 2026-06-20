# Agent instructions

## Cursor Cloud specific instructions

Cloud agents must test the application in Docker containers. Do not install or run
Tomcat, PostgreSQL, or other app services directly on the Cursor host machine.

### Prerequisites

This repository configures the Cloud Agent environment via:

- `.cursor/Dockerfile` — installs Docker CLI, Docker Compose, Java 17, and Maven
- `.cursor/environment.json` — starts the Docker daemon on agent boot

If `docker` is missing, confirm `.cursor/environment.json` is committed and no stale
saved environment snapshot is overriding it in the Cloud Agents dashboard.

### Integration and end-to-end testing

Always use Docker Compose for app + PostgreSQL testing:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f app
curl http://localhost:8080/db-demo
docker compose down
```

The response from `/db-demo` should contain `Connected to Render PostgreSQL` and
`Heartbeat rows`.

See `src/docs/local-docker-compose.md` and `README.DOCKER.md` for details.

### Unit tests

Maven unit tests may run on the host:

```bash
mvn test
```

`DatabaseDemoJpaIntegrationTest` uses an in-memory H2 database and does not require
Docker. Do not install PostgreSQL on the host for integration testing.

### Cleanup

Always stop containers after testing:

```bash
docker compose down
```

To reset the local database volume:

```bash
docker compose down -v
```
