Start the application against MySQL via Docker Compose.

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Required env vars: `DB_USERNAME`, `DB_PASSWORD` (and optionally `MYSQL_HOST`, defaults to `localhost`).
Swagger UI: http://localhost:8080/swagger-ui/index.html
