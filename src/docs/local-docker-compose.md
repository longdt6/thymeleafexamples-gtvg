# Local Docker Compose Database Environment

Use Docker Compose when you want to test the application and PostgreSQL together
without depending on Render secrets.

## Services

`docker-compose.yml` starts two containers:

- `db`: PostgreSQL 18
- `app`: the Spring Boot/Spring MVC WAR built from this repository's
  `Dockerfile` and deployed to Tomcat 10.1

The app receives this environment variable:

```text
DATABASE_URL=postgresql://gtvg:gtvg@db:5432/gtvg?sslmode=disable
```

The hostname `db` is the Compose service name. It only works inside the Compose
network.

## Start the environment

Run from the repository root:

```bash
docker compose up --build
```

Wait until the app logs show Tomcat has started, then open:

```text
http://localhost:8080/db-demo
```

Expected result:

- The page shows `Connected to Render PostgreSQL`.
- The page mentions Spring Data JPA and Hibernate.
- `Heartbeat rows` increases when the page is refreshed.

The label still says Render PostgreSQL because the same code path is used for
local PostgreSQL and Render PostgreSQL. The local Compose database proves that
the app works when `DATABASE_URL` is configured.

## Useful commands

Start in the foreground:

```bash
docker compose up --build
```

Start in the background:

```bash
docker compose up --build -d
```

View logs:

```bash
docker compose logs -f app
docker compose logs -f db
```

Stop containers:

```bash
docker compose down
```

Stop containers and delete the local database volume:

```bash
docker compose down -v
```

## Quick verification

After the environment starts:

```bash
curl http://localhost:8080/db-demo
```

The response should contain:

```text
Connected to Render PostgreSQL
Heartbeat rows
```

## Production comparison

Local Compose:

- Uses local container DNS: `db`
- Uses `sslmode=disable`
- Uses demo credentials from `docker-compose.yml`

Render production:

- Uses Render's internal PostgreSQL connection string
- Stores the value in the Render web service environment as `DATABASE_URL`
- Should not commit real credentials to the repository

## Troubleshooting

### The app still says `DATABASE_URL is not configured yet`

- Confirm you started the app with Compose, not only the Dockerfile image.
- Run `docker compose config` and check that `app.environment.DATABASE_URL` is
  present.
- Recreate the app container with `docker compose up --build --force-recreate`.

### The app cannot connect to PostgreSQL

- Check database health with `docker compose ps`.
- Check database logs with `docker compose logs db`.
- Confirm `DATABASE_URL` uses host `db`, not `localhost`, inside Compose.

### The database data looks stale

The Compose file uses a named volume: `gtvg-postgres-data`.

Reset the database:

```bash
docker compose down -v
docker compose up --build
```
