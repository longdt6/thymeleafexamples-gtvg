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

When `docker-compose.yml` exists at the repository root:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f app
curl http://localhost:8080/db-demo
docker compose down
```

See `src/docs/local-docker-compose.md` when that file is present.

When only the root `Dockerfile` is available:

```bash
docker build -t thymeleaf-gtvg:latest .
docker run --rm -d -p 8080:8080 --name thymeleaf-gtvg thymeleaf-gtvg:latest
curl http://localhost:8080/
docker stop thymeleaf-gtvg
```

See `README.DOCKER.md` for details.

### Unit tests

Maven unit tests may run on the host:

```bash
mvn test
```

Do not install PostgreSQL on the host for integration testing. Use Docker Compose
or a containerized database instead.

### Cleanup

Always stop containers after testing:

```bash
docker compose down
# or, for single-container runs:
docker stop thymeleaf-gtvg
```
