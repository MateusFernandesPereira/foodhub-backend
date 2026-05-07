# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FoodHub Backend is a Maven multi-module monorepo of Spring Boot microservices for a food ordering and delivery platform. Java 17, Spring Boot 3.5.x. It is a portfolio project — always follow current industry best practices and modern architecture patterns.

**Current state:** `catalog-service` is partially implemented. All other services are planned.

---

## Commands

All Maven commands should be run from the repo root using the wrapper (`./mvnw`).

```bash
# Build all modules (skip tests)
./mvnw clean package -DskipTests

# Build a specific module
./mvnw clean package -pl catalog-service -DskipTests

# Run all tests (requires Docker for Testcontainers)
./mvnw test

# Run tests for a specific module
./mvnw test -pl catalog-service

# Run a single test class
./mvnw test -pl catalog-service -Dtest=CatalogServiceApplicationTests

# Start the app (after starting infrastructure)
./mvnw spring-boot:run -pl catalog-service

# Start infrastructure (PostgreSQL on 5432, Redis on 6379)
docker compose up -d
```

---

## Target Architecture

```
Client Apps
     │
     ▼
[api-gateway :8080]  — Spring Cloud Gateway, JWT auth filter, rate limiting
     │
     ├──► [catalog-service  :8081]  — PostgreSQL (catalog_db) + Redis
     ├──► [auth-service     :8082]  — PostgreSQL (auth_db), JWT issuer
     ├──► [orders-service   :8083]  — PostgreSQL (orders_db) + Kafka producer
     ├──► [payments-service :8084]  — PostgreSQL (payments_db) + Kafka consumer/producer
     ├──► [delivery-service :8085]  — PostgreSQL (delivery_db)
     └──► [notifications-service :8086]  — Kafka consumer, email/push

[analytics-service :8087]  — MongoDB, reads all domain events from Kafka

Infrastructure:
  Apache Kafka + Zookeeper
  PostgreSQL 16 (one instance per service, separate databases)
  Redis 7 (cache)
  MongoDB (analytics)
  Zipkin (distributed tracing)
  Prometheus + Grafana (metrics)
```

---

## Module structure

Each service is a Maven module under the root `pom.xml`. The root POM sets the Spring Boot parent and shared properties; service POMs inherit from it.

**Adding a new service:**
1. Create the directory: `<service-name>/`
2. Add a `pom.xml` inheriting from the root with `<parent><artifactId>foodhub-backend</artifactId></parent>`
3. Add the module to the root `pom.xml` `<modules>` section
4. Add a `Dockerfile` following the multi-stage pattern from `catalog-service/Dockerfile`
5. Add the service to `docker-compose.yml`

---

## Layered package structure

Every service follows this exact package layout under `com.pereira.<service-name>`:

```
controller/   — @RestController classes + GlobalExceptionHandler (@RestControllerAdvice)
service/      — interface definitions only
service/impl/ — @Service implementations
repository/   — Spring Data JPA or reactive repositories
entity/       — JPA entities (or MongoDB documents for analytics-service)
dto/
  request/    — Java records with Bean Validation annotations
  response/   — Java records with a static factory method .from(entity)
config/       — Spring @Configuration classes (security, cache, kafka, etc.)
event/        — Kafka event payload records (for producer/consumer services)
exception/    — Custom exception classes
```

---

## Key conventions

### DTOs
- **DTOs are Java records.** No mapper libraries (MapStruct, ModelMapper).
- Request records carry `@Valid` constraints directly on fields.
- Response records have a `static XxxResponse from(XxxEntity entity)` factory method.
- Error responses use a shared structured format — see the Error handling section below.

### Entities
- **Soft delete via Hibernate annotations.** Entities that support deletion use `@SQLDelete(sql = "UPDATE table SET deleted_at = NOW() WHERE id = ?")` and `@SQLRestriction("deleted_at IS NULL")`. Never hard-delete these entities.
- All entities use `@CreationTimestamp` / `@UpdateTimestamp` for `created_at` / `updated_at`.
- Use `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor` via Lombok.
- Use `GenerationType.IDENTITY` for primary keys (PostgreSQL `BIGSERIAL`).

### Database
- **Schema managed by Flyway.** Migration scripts live in `src/main/resources/db/migration/` following the `V<n>__description.sql` naming convention.
- `ddl-auto` is always `validate` — never `update` or `create`. Schema changes require a new migration file.
- Each service has its own isolated database. Cross-service data access goes through REST or Kafka, never direct DB access.

### Error handling
- `GlobalExceptionHandler` in every service handles all exceptions uniformly.
- Error response body is a structured record, not a plain string:
  ```java
  record ErrorResponse(int status, String error, String message, String path, LocalDateTime timestamp) {}
  ```
- Handle at minimum: `EntityNotFoundException` (404), `MethodArgumentNotValidException` (400 with field details), generic `Exception` (500).

### REST API conventions
- Base path: `/api/v1/<resource>` (plural noun).
- Use `ResponseEntity<T>` return types on all controller methods.
- Pagination via `Pageable` (Spring Data Web) with `@PageableDefault`.
- Collection endpoints always return `Page<T>` (never raw `List<T>`).
- Use appropriate HTTP status codes: 201 for POST (create), 204 for DELETE/PATCH (no content), 200 for GET/PUT.

### Caching (Redis)
- Use `@EnableCaching` + `@Cacheable` / `@CacheEvict` annotations.
- Cache names follow the pattern `<service>:<resource>` (e.g., `catalog:products`).
- Always evict on update and delete operations.

### Kafka events
- Event payload records live in `event/` package.
- Topic naming: `<domain>.<event-past-tense>` (e.g., `order.created`, `payment.processed`).
- Use the **Outbox pattern** in services that publish events to guarantee at-least-once delivery.
- Consumers use `@KafkaListener` with explicit `groupId` and `@Transactional` where needed.

### Inter-service communication
- Synchronous calls use **Spring Cloud OpenFeign** clients.
- Wrap OpenFeign calls with **Resilience4j circuit breakers** to handle downstream failures.
- Async communication always goes through **Kafka**.

### Security
- `auth-service` is the sole JWT issuer.
- All other services validate JWT tokens. In the final architecture, validation happens at `api-gateway` level.
- Roles: `ROLE_ADMIN` (manage catalog, view all orders) and `ROLE_CUSTOMER` (place orders, view own orders).

### Observability
- All services include `spring-boot-starter-actuator`.
- Prometheus endpoint exposed at `/actuator/prometheus` (add Micrometer Prometheus dependency).
- Distributed tracing with `micrometer-tracing-bridge-brave` + Zipkin reporter.
- Log format: structured JSON in production profiles (Logback with `logstash-logback-encoder`).

### Tests
- **Testcontainers is mandatory.** Never mock the database in integration tests. `TestcontainersConfiguration` provides real containers via `@ServiceConnection`.
- Unit tests: service layer logic only, mock the repository layer with Mockito.
- Integration tests: use `@SpringBootTest` + `MockMvc` / `WebTestClient` for controller tests.
- Test classes: `XxxControllerTest` (MockMvc), `XxxServiceTest` (unit), `XxxRepositoryTest` (data layer).
- Minimum coverage target: 80% on service and controller layers.

---

## Infrastructure (docker-compose.yml)

| Service | Image | Port | Notes |
|---------|-------|------|-------|
| postgres | postgres:16-alpine | 5432 | Shared instance, separate DBs per service |
| redis | redis:7-alpine | 6379 | Shared cache instance |
| kafka | confluentinc/cp-kafka | 9092 | Requires zookeeper |
| zookeeper | confluentinc/cp-zookeeper | 2181 | Kafka dependency |
| mongodb | mongo:7 | 27017 | analytics-service only |
| zipkin | openzipkin/zipkin | 9411 | Distributed tracing UI |
| prometheus | prom/prometheus | 9090 | Metrics scraping |
| grafana | grafana/grafana | 3000 | Dashboards |

---

## Service roadmap and dependencies

| Phase | Service | Depends on | Key patterns |
|-------|---------|------------|--------------|
| 1 (current) | catalog-service | PostgreSQL, Redis | CRUD, soft delete, caching, OpenAPI |
| 2 | auth-service | PostgreSQL | JWT, Spring Security, roles |
| 3 | orders-service | catalog-service (Feign), Kafka | FSM, Outbox pattern, events |
| 4 | api-gateway | auth-service | Spring Cloud Gateway, JWT filter |
| 5 | payments-service | Kafka | Event consumer/producer |
| 6 | notifications-service | Kafka | Email simulation |
| 7 | delivery-service | Kafka | Status tracking |
| 8 | analytics-service | Kafka, MongoDB | CQRS read side |

---

## Design decisions

- **One PostgreSQL database per service** — enforces bounded context isolation. Cross-service queries go through the API, not direct DB joins.
- **Kafka over RabbitMQ for domain events** — Kafka's log-based storage enables replay and analytics-service to consume the full event history.
- **Soft delete** — preserves data integrity for order history (a deleted product must still be readable in historical orders).
- **No mapper library** — static factory methods on response records are explicit, easy to trace, and avoid magic. Acceptable trade-off at this project scale.
- **JWT stateless auth** — no session store needed; scales horizontally. Short-lived access tokens (15 min) + refresh tokens (7 days).
- **Outbox pattern** — prevents dual-write problem between DB commit and Kafka publish. A scheduled job reads the outbox table and publishes pending events.

---

## Current service status

### catalog-service — Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/products` | Create product |
| GET | `/api/v1/products` | List products (paginated) |
| GET | `/api/v1/products/{id}` | Get product by ID |
| PUT | `/api/v1/products/{id}` | Update product (partial) |
| DELETE | `/api/v1/products/{id}` | Soft delete |
| PATCH | `/api/v1/products/{id}` | Restore soft-deleted product |

### catalog-service — Pending improvements

- Structured `ErrorResponse` record (currently returns plain `String` on 404)
- Field-level validation errors for 400 responses
- Query filters: `?category=`, `?available=`, `?minPrice=`, `?maxPrice=`, `?name=`
- Redis caching on GET endpoints
- OpenAPI documentation (springdoc-openapi)
- Spring Actuator + Prometheus metrics
- Full integration test coverage (currently only `contextLoads()`)
