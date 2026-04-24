Start the application in dev mode (H2 in-memory database).

```bash
./mvnw spring-boot:run
```

Once started, the following URLs are available:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 Console: http://localhost:8080/h2-console  (JDBC URL: `jdbc:h2:mem:appDb`, user: `sa`, password: `password`)

Seed credentials (all share the same password — check `data.sql`):
- `admin@bookshop.com` / ADMINISTRATOR
- `manager@bookshop.com` / MANAGER
- `john.doe@example.com` / CUSTOMER
