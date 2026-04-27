# Bookshop API

A RESTful web service for a bookshop application, providing management for products, users, and bookings.

## Tech Stack

- **Language:** Java 25
- **Framework:** Spring Boot 3.5.14
- **Build Tool:** Maven
- **Persistence:** Spring Data JPA, Hibernate, Flyway
- **Database:**
  - **H2** (in-memory, local development)
  - **MySQL 5.7** (production and integration tests via Testcontainers)
- **Security:** Spring Security with JWT
- **API Documentation:** SpringDoc OpenAPI (Swagger UI)
- **Testing:** JUnit 5, Mockito, AssertJ, Allure, Testcontainers
- **Other:** Lombok, MapStruct, Docker Compose

## Project Structure

```text
com.bookshop
├── config         # Configuration classes (Security, OpenAPI)
├── controller     # REST Controllers and API route constants (ApiRoutes)
├── converter      # Manual Entity <-> DTO mappers
├── dto            # Data Transfer Objects with validation
├── exception      # Custom exceptions and GlobalExceptionHandler
├── model          # JPA Entities and Enums
├── repository     # Spring Data JPA Repositories
├── security       # JWT logic and security filters
├── service        # Business logic
└── util           # Shared utility classes
```

## Architecture

### Application Layers

```
HTTP Request
     │
     ▼
┌─────────────┐
│  Controller │  REST endpoints, input validation, OpenAPI annotations
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Service   │  Business logic, transaction boundaries
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Converter  │  Manual Entity ↔ DTO mapping (@Component beans)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Repository  │  Spring Data JPA interfaces
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Database   │  H2 (dev) / MySQL (prod)
└─────────────┘
```

Cross-cutting: `JwtAuthFilter` → `Spring Security` → `GlobalExceptionHandler`

### Database Schema

```
┌──────────────────────┐        ┌──────────────────────┐
│        users         │        │       products        │
├──────────────────────┤        ├──────────────────────┤
│ id          BIGINT PK│        │ id          BIGINT PK │
│ name        VARCHAR  │        │ title       VARCHAR   │
│ email       VARCHAR  │        │ author      VARCHAR   │
│ password    VARCHAR  │        │ description VARCHAR   │
│ role        ENUM     │        │ price       DECIMAL   │
│  CUSTOMER            │        │ quantity    INT        │
│  MANAGER             │        └──────────┬───────────┘
│  ADMINISTRATOR       │                   │
└──────────┬───────────┘                   │
           │                               │
           │      ┌────────────────────────┐
           │      │       bookings         │
           │      ├────────────────────────┤
           └─────►│ user_id    BIGINT FK   │◄─────┘
                  │ product_id BIGINT FK   │
                  │ quantity   INT         │
                  │ status     ENUM        │
                  │  PENDING               │
                  │  APPROVED              │
                  │  REJECTED              │
                  │  CANCELLED             │
                  │ created_at TIMESTAMP   │
                  └────────────────────────┘
```

## Requirements

- **Java:** JDK 25
- **Maven:** 3.9+ (via Maven Wrapper — no local Maven install required)
- **Docker & Docker Compose:** Required for integration tests and production profile

## Setup & Run

### Local Development (H2, default)

Runs with H2 in-memory database and the `dev` profile:

```bash
./mvnw spring-boot:run
```

- **Port:** 8080
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **H2 Console:** http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:appDb`)

### Docker Compose (MySQL + App)

Builds the production image and starts both the database and the application:

1. Copy the environment template and fill in credentials:
   ```bash
   cp .env.example .env
   # edit .env — set MYSQL_ROOT_PASSWORD, MYSQL_USER, MYSQL_PASSWORD, APP_SECURITY_JWT_SECRET
   ```
2. Start everything:
   ```bash
   docker compose up --build
   ```

The `app` service waits for the `db` healthcheck to pass before starting. Seed data is applied automatically via Flyway on first startup.

- **Port:** 8080
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html

### Running the App Against MySQL Without Docker Build

Start only the database, then run the app via Maven with the `prod` profile:

```bash
docker compose up -d db

# bash / WSL
DB_USERNAME=<user> DB_PASSWORD=<pass> ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# PowerShell
$env:DB_USERNAME="<user>"; $env:DB_PASSWORD="<pass>"; ./mvnw spring-boot:run "-Dspring-boot.run.profiles=prod"
```

> **Note:** Use `-Dspring-boot.run.profiles=prod` (not `-Dspring.profiles.active=prod`) when running via the Maven plugin — the latter is ignored by the Spring Boot Maven plugin.

## Running Tests

Integration tests use **Testcontainers** — they spin up a real MySQL 5.7 container automatically. **Docker must be running** before executing the test suite.

```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw -Dtest=ClassName test

# Run tests + enforce JaCoCo coverage thresholds (≥70% instruction, ≥50% branch)
./mvnw verify
```

### Testcontainers on Rancher Desktop (Windows)

If you use Rancher Desktop instead of Docker Desktop, create `~/.testcontainers.properties` with:

```properties
docker.host=npipe:////./pipe/docker_engine
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | MySQL username | *(required in prod)* |
| `DB_PASSWORD` | MySQL password | *(required in prod)* |
| `MYSQL_HOST` | MySQL host | `localhost` |
| `APP_SECURITY_JWT_SECRET` | Secret key for JWT signing | `devSecretKey-32bytes-or-longer!!` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |

All production credentials are loaded from `.env` (gitignored). Use `.env.example` as the template.

## Scripts

- **Build project:** `./mvnw clean install`
- **Run all tests:** `./mvnw test`
- **Run specific test class:** `./mvnw -Dtest=ClassName test`
- **Generate Allure report:**
  ```bash
  ./mvnw allure:serve
  ```

## Testing

- **Unit Tests:** `src/test/java/com/bookshop/service/` — Mockito-based, no container required.
- **Integration Tests:** `src/test/java/com/bookshop/controller/` — `MockMvc` + `@SpringBootTest` backed by a real MySQL 5.7 container via Testcontainers.
  - All controller test classes extend `AbstractIntegrationTest`.
  - `TestContainersConfig` uses `@ServiceConnection` to auto-wire the datasource from the container.
  - The `dev` Spring profile is activated during tests; Testcontainers overrides the datasource URL.
- **Database Isolation:** Integration tests use `@Transactional` to roll back after each test.
- **CVE note:** `commons-compress` is pinned to `1.27.1` in `dependencyManagement` to address CVE-2024-25710 and CVE-2024-26308 (transitive via Testcontainers).

## Docker Image

The `Dockerfile` uses a two-stage build:

- **Stage 1** (`eclipse-temurin:25-jdk-alpine`): Maven build with BuildKit cache mount for the local Maven repository, then layered-jar extraction.
- **Stage 2** (`eclipse-temurin:25-jre-alpine`): Minimal runtime image (~131 MB), non-root user `bookshop`, `devtools` and `h2` excluded from the production jar.

Build the image standalone:

```bash
docker build -t bookshop-api .
```

## API Endpoints

### Authentication — `/api/auth`
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/auth/login` | Login, returns JWT | Public |
| POST | `/api/auth/register` | Register new user | Public |

### Users — `/api/users`
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/users` | List all users | MANAGER, ADMINISTRATOR |
| GET | `/api/users/{id}` | Get user by ID | Owner, MANAGER, ADMINISTRATOR |
| PUT | `/api/users/{id}` | Update user | MANAGER, ADMINISTRATOR |
| DELETE | `/api/users/{id}` | Delete user | ADMINISTRATOR |

### Products — `/api/products`
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/products` | List all products (pageable) | Public |
| GET | `/api/products/{id}` | Get product by ID | Public |
| POST | `/api/products` | Create product | MANAGER, ADMINISTRATOR |
| PATCH | `/api/products/{id}` | Update product (partial) | MANAGER, ADMINISTRATOR |
| DELETE | `/api/products/{id}` | Delete product (blocked if bookings exist) | ADMINISTRATOR |

### Bookings — `/api/bookings`
| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/bookings` | List all bookings (pageable) | MANAGER, ADMINISTRATOR |
| GET | `/api/bookings/{id}` | Get booking by ID | Owner, MANAGER, ADMINISTRATOR |
| GET | `/api/bookings/user/{userId}` | Get bookings by user | Owner, MANAGER, ADMINISTRATOR |
| POST | `/api/bookings` | Create booking (status: PENDING) | Any authenticated |
| PATCH | `/api/bookings/{id}` | Update booking status `{"status": "APPROVED"}` | MANAGER, ADMINISTRATOR |
| DELETE | `/api/bookings/{id}` | Delete booking | ADMINISTRATOR |

**Booking statuses:** `PENDING` → `APPROVED` / `REJECTED` / `CANCELLED`

## Error Handling

All errors follow a consistent response format:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 5",
  "timestamp": "2026-04-22T21:00:00"
}
```

| Scenario | HTTP Status |
|----------|-------------|
| Resource not found | 404 Not Found |
| Duplicate resource | 409 Conflict |
| Business rule violation | 409 Conflict |
| Validation failure | 400 Bad Request |
| Malformed JSON | 400 Bad Request |
| Missing required parameter | 400 Bad Request |
| Invalid sort field | 400 Bad Request |
| Unauthorized | 401 Unauthorized |
| Forbidden | 403 Forbidden |

## Bruno Collection

A Bruno collection is available in `bruno-collection/` for API testing. Import it directly into [Bruno](https://usebruno.com) and configure the `local` environment.
