# Docker for thymeleafexamples-gtvg

This repository contains a multi-stage Dockerfile that builds the Spring Boot/Spring MVC WAR with Maven and runs it on Tomcat 10.1.

Build the image (run from repository root):

```bash
# build the Maven project and create Docker image
docker build -t thymeleaf-gtvg:latest .
```

Run the app only:

```bash
docker run --rm -p 8080:8080 --name thymeleaf-gtvg thymeleaf-gtvg:latest
```

Run the app only with a PostgreSQL database URL:

```bash
docker run --rm -p 8080:8080 \
  -e DATABASE_URL="postgresql://user:password@host:5432/database?sslmode=require" \
  --name thymeleaf-gtvg thymeleaf-gtvg:latest
```

Open `/db-demo` to verify that the application can connect to PostgreSQL through
Spring Data JPA and Hibernate, persist a heartbeat entity in the
`render_demo_heartbeat` table, and read database metadata.

Run the full local app + PostgreSQL environment:

```bash
docker compose up --build
```

Then open:

```text
http://localhost:8080/db-demo
```

See `src/docs/local-docker-compose.md` for details.

Render setup:

1. Create a PostgreSQL database on Render.
2. Copy the database's internal connection string.
3. Add it to the `thymeleafexamples-gtvg` web service as `DATABASE_URL`.
4. Redeploy the web service and open `/db-demo`.

If you use the external connection string, append `sslmode=require` to the URL.

Notes:
- The application is a Spring Boot/Spring MVC WAR deployed to Tomcat 10.
- The Dockerfile builds the WAR using the `maven:3.9.9-eclipse-temurin-17` image and then deploys it to `tomcat:10.1-jre17-temurin` as `ROOT.war`.
- If you need to include external configs (datasource via JNDI), mount a `context.xml` into `/usr/local/tomcat/conf/context.xml` or configure a custom `server.xml`/JNDI resource as needed.

Troubleshooting:
- If the build fails due to missing dependencies, ensure you have network access to Maven central or configured repositories.
- For local development you can build the WAR first and then use a simple Tomcat image: `docker run -v $(pwd)/target:...` (see Tomcat docs).
