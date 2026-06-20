# Render Production Database Configuration

This project runs as a Spring Boot/Spring MVC web application and can connect to
a Render PostgreSQL database in production through the `DATABASE_URL`
environment variable. The database demo uses Spring Data JPA and Hibernate to
persist rows in the `render_demo_heartbeat` table.

## Current Render resources

- Web service: `thymeleafexamples-gtvg`
- Web service URL: `https://thymeleafexamples-gtvg.onrender.com`
- Web service runtime: Docker
- Web service region: Singapore
- Web service deploy branch: `3.1-master-jakarta`
- PostgreSQL database: `smeconnect-demo-db`
- PostgreSQL database name: `smeconnect_demo_db`
- PostgreSQL database user: `smeconnect_demo_db_user`
- PostgreSQL region: Singapore

## Deployment checklist

1. Merge the database demo code into the branch deployed by Render.
   - The current Render service deploys `3.1-master-jakarta`.
   - If the code is still on a feature branch, merge the PR before relying on
     the production service.

2. Open the Render dashboard.
   - Go to the `thymeleafexamples-gtvg` web service.
   - Open the `Environment` section.

3. Add the database connection string.
   - Key: `DATABASE_URL`
   - Value: copy the **Internal Database URL** from the Render PostgreSQL
     database `smeconnect-demo-db`.
   - Prefer the internal URL when the web service and database are in the same
     Render workspace and region.

4. Save the environment variable and redeploy the web service.
   - Render needs a new deploy so Tomcat starts with the new environment.
   - The app reads `DATABASE_URL` only from the process environment.

5. Verify the deployment.
   - Open `https://thymeleafexamples-gtvg.onrender.com/db-demo`.
   - Expected success state:
     - The page shows `Connected to Render PostgreSQL`.
     - The page mentions Spring Data JPA and Hibernate.
     - `Heartbeat rows` increases when the page is refreshed.

## External connection URL

If you use Render's external database URL instead of the internal URL, require
SSL explicitly:

```text
postgresql://USER:PASSWORD@HOST:5432/DATABASE?sslmode=require
```

Do not commit the real URL or password to the repository.

## How the application uses the database

- Spring MVC controllers handle routes such as `/`, `/db-demo`,
  `/product/list`, and `/order/list`.
- `DatabaseDemoService` is a Spring `@Service` and reads `DATABASE_URL` from the
  Spring environment.
- `DatabaseDemoPersistenceConfig` is enabled only when `DATABASE_URL` is set.
  It creates a HikariCP datasource, an `EntityManagerFactory`, Spring Data JPA
  repositories, and a transaction manager.
- `RenderDemoHeartbeatRepository` persists heartbeat rows through Spring Data
  JPA.
- Hibernate currently uses `hibernate.hbm2ddl.auto=update` for this demo so the
  `render_demo_heartbeat` table can be created automatically.

## Production notes

- The current `smeconnect-demo-db` is on Render's free PostgreSQL plan and is
  suitable only for demo usage.
- For real production usage, move the database to a paid Render PostgreSQL plan
  before depending on it.
- For a long-lived production schema, replace Hibernate auto-update with an
  explicit migration tool such as Flyway or Liquibase.
- Keep `DATABASE_URL` configured only in Render environment variables or another
  secret manager.

## Troubleshooting

### The page still says `DATABASE_URL is not configured yet`

- Confirm the `DATABASE_URL` key exists on the `thymeleafexamples-gtvg` web
  service, not only on the database.
- Redeploy the web service after saving the environment variable.
- Confirm the deployed branch contains the database demo code.

### The page says it cannot connect to PostgreSQL

- Confirm the URL was copied from the correct database.
- If using the external URL, confirm `sslmode=require` is present.
- Confirm the database status in Render is `available`.
- Check Render service logs for the full connection error.

### The app deploys but `/db-demo` is missing

- Confirm the deployed branch includes the `/db-demo` route.
- Confirm Render is deploying the expected branch (`3.1-master-jakarta` by
  default for the current service).
