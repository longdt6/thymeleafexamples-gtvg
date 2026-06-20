# Docker for thymeleafexamples-gtvg

This repository contains a multi-stage Dockerfile that builds the project with Maven and runs it on Tomcat 10 (which supports Jakarta Servlet API 5.0 used in this project).

Build the image (run from repository root):

```bash
# build the Maven project and create Docker image
docker build -t thymeleaf-gtvg:latest .
```

Run the container:

```bash
docker run --rm -p 8080:8080 --name thymeleaf-gtvg thymeleaf-gtvg:latest
```

Run the container with a Render PostgreSQL database demo:

```bash
docker run --rm -p 8080:8080 \
  -e DATABASE_URL="postgresql://user:password@host:5432/database?sslmode=require" \
  --name thymeleaf-gtvg thymeleaf-gtvg:latest
```

Open `/db-demo` to verify that the application can connect to PostgreSQL, create the
`render_demo_heartbeat` table, insert a heartbeat row, and read database metadata.

Render setup:

1. Create a PostgreSQL database on Render.
2. Copy the database's internal connection string.
3. Add it to the `thymeleafexamples-gtvg` web service as `DATABASE_URL`.
4. Redeploy the web service and open `/db-demo`.

If you use the external connection string, append `sslmode=require` to the URL.

Notes:
- The `pom.xml` uses `jakarta.servlet:jakarta.servlet-api:5.0.0` (scope provided). Tomcat 10+ is required because it implements Jakarta Servlet 5.
- The Dockerfile builds the WAR using the `maven:3.8.8` image and then deploys it to `tomcat:10` as `ROOT.war`.
- If you need to include external configs (datasource via JNDI), mount a `context.xml` into `/usr/local/tomcat/conf/context.xml` or configure a custom `server.xml`/JNDI resource as needed.

Troubleshooting:
- If the build fails due to missing dependencies, ensure you have network access to Maven central or configured repositories.
- For local development you can build the WAR first and then use a simple Tomcat image: `docker run -v $(pwd)/target:...` (see Tomcat docs).
