# Bookshop API

A RESTful web service for a bookshop application, providing management for products, users, and bookings.

## 🚀 Tech Stack

- **Language:** Java 25
- **Framework:** Spring Boot 3.5.3
- **Build Tool:** Maven
- **Persistence:** Spring Data JPA, Hibernate
- **Database:**
  - **H2** (In-memory, for development and testing)
  - **MySQL** (For production-like environments)
- **Security:** Spring Security with JWT (JSON Web Token)
- **API Documentation:** SpringDoc OpenAPI (Swagger UI)
- **Testing:** JUnit 5, Mockito, AssertJ, Allure
- **Other:** Lombok, Docker Compose

## 📁 Project Structure

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

## 📋 Requirements

- **Java:** JDK 25
- **Maven:** 3.9.9 (via Maven Wrapper — no local Maven install required)
- **Docker & Docker Compose:** Required for running the MySQL database in `prod` profile.

## 🛠️ Setup & Run

### Development (Default)
Runs with H2 in-memory database and `dev` profile.
```bash
./mvnw spring-boot:run
```
- **Port:** 8080
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:appDb`)

### Production-like
Uses MySQL database via Docker.
1. Start the database:
   ```bash
   docker compose up -d
   ```
2. Run the application with `prod` profile:
   ```bash
   ./mvnw spring-boot:run -Dspring.profiles.active=prod
   ```

## 🔐 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `APP_SECURITY_JWT_SECRET` | Secret key for JWT signing | `devSecretKey-32bytes-or-longer!!` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |

## 📜 Scripts

- **Build project:** `./mvnw clean install`
- **Run all tests:** `./mvnw test`
- **Run specific test class:** `./mvnw -Dtest=ClassName test`
- **Generate Allure report:**
  ```bash
  ./mvnw allure:serve
  ```

## 🧪 Testing

- **Unit Tests:** `src/test/java/com/bookshop/service/` — uses Mockito for mocking dependencies.
- **Integration Tests:** `src/test/java/com/bookshop/controller/` — uses `MockMvc` and `@SpringBootTest`.
- **Database Isolation:** Integration tests use `@Transactional` to roll back after each test.

## 📡 API Endpoints

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

## ⚠️ Error Handling

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

## 🗂️ Bruno Collection

A Bruno collection is available in `bruno-collection/` for API testing. Import it directly into [Bruno](https://usebruno.com) and configure the `local` environment.

## ✅ TODOs
- [ ] Set up CI/CD pipeline (e.g., GitHub Actions).

## 📄 License
TODO: Add license information.
