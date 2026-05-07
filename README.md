<div align="center">

# FoodHub Backend

**Plataforma de pedidos e entregas construída com arquitetura de microsserviços em Spring Boot**

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?style=flat-square&logo=springboot)
![Maven](https://img.shields.io/badge/Maven-Multi--module-red?style=flat-square&logo=apachemaven)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![Kafka](https://img.shields.io/badge/Kafka-Events-black?style=flat-square&logo=apachekafka)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

</div>

---

## Sobre o projeto

FoodHub Backend é um projeto de portfólio que simula o back-end de uma plataforma de pedidos e entregas de comida. O objetivo é demonstrar na prática os principais padrões e tecnologias utilizados no mercado de desenvolvimento back-end com Java e Spring Boot, incluindo arquitetura de microsserviços, mensageria assíncrona, observabilidade e boas práticas de qualidade de código.

### Objetivos de aprendizado e demonstração

- Arquitetura de microsserviços com comunicação síncrona (REST via OpenFeign) e assíncrona (Kafka)
- API Gateway centralizado com autenticação JWT
- Persistência poliglota: PostgreSQL por serviço + MongoDB para analytics
- Padrão Outbox para confiabilidade na publicação de eventos
- Cache distribuído com Redis
- Observabilidade: métricas (Prometheus/Grafana), rastreamento distribuído (Zipkin), logs estruturados
- Testes de integração reais com Testcontainers (sem mocks de infraestrutura)
- CI/CD com GitHub Actions

---

## Arquitetura

```
                         ┌─────────────────────────────┐
                         │        Client Apps           │
                         │   (Web / Mobile / Postman)   │
                         └──────────────┬──────────────┘
                                        │
                                        ▼
                         ┌─────────────────────────────┐
                         │   API Gateway  :8080         │
                         │  Spring Cloud Gateway        │
                         │  (roteamento + auth JWT)     │
                         └──┬──────┬──────┬──────┬─────┘
                            │      │      │      │
               ┌────────────┘      │      │      └──────────────┐
               ▼                   ▼      ▼                     ▼
  ┌────────────────────┐  ┌──────────┐ ┌──────────────┐ ┌──────────────────┐
  │  catalog-service   │  │  auth-   │ │   orders-    │ │  payments-       │
  │     :8081          │  │ service  │ │   service    │ │  service         │
  │  Catálogo de       │  │  :8082   │ │    :8083     │ │   :8084          │
  │  produtos          │  │  JWT +   │ │  Pedidos +   │ │  Simulação de    │
  │                    │  │  Roles   │ │  FSM status  │ │  pagamentos      │
  └────────┬───────────┘  └────┬─────┘ └──────┬───────┘ └───────┬──────────┘
           │                   │              │                  │
           ▼                   ▼              │                  │
      PostgreSQL           PostgreSQL         │    ┌─────────────▼─────────────┐
      (catalog_db)         (auth_db)          │    │         Apache Kafka       │
      Redis (cache)                           └───►│                           │
                                                   │  Topics:                  │
  ┌────────────────────┐  ┌──────────────────┐    │  order.created            │
  │ delivery-service   │  │notifications-    │    │  order.status.updated     │
  │     :8085          │◄─┤  service :8086   │◄───│  payment.processed        │
  │  Rastreio de       │  │  Email/Push via  │    └───────────────────────────┘
  │  entregas          │  │  Kafka consumer  │
  └────────┬───────────┘  └──────────────────┘
           │
           ▼                ┌──────────────────────┐
      PostgreSQL            │  analytics-service   │
      (delivery_db)         │      :8087           │
                            │  Eventos → MongoDB   │
                            └──────────┬───────────┘
                                       ▼
                                   MongoDB

Observabilidade:
  Prometheus + Grafana (métricas) | Zipkin (rastreamento) | Logs estruturados (JSON)
```

---

## Stack tecnológica

| Categoria | Tecnologia | Uso |
|-----------|-----------|-----|
| **Linguagem** | Java 17 | Versão LTS com records, sealed classes, pattern matching |
| **Framework** | Spring Boot 3.5 | Base de todos os serviços |
| **Persistência** | Spring Data JPA + Hibernate | ORM nos serviços relacionais |
| **Schema** | Flyway | Migrations versionadas por serviço |
| **Banco relacional** | PostgreSQL 16 | Um banco por serviço (isolamento) |
| **Banco documental** | MongoDB | analytics-service |
| **Cache** | Redis 7 | Cache de leitura no catalog-service |
| **Mensageria** | Apache Kafka | Eventos assíncronos entre serviços |
| **API Gateway** | Spring Cloud Gateway | Roteamento e autenticação JWT centralizada |
| **Comunicação REST** | Spring Cloud OpenFeign | Chamadas síncronas entre serviços |
| **Segurança** | Spring Security + JWT | Autenticação stateless |
| **Validação** | Jakarta Bean Validation | Validação de request bodies |
| **Testes** | JUnit 5 + Testcontainers | Testes de integração com infra real |
| **Documentação** | SpringDoc OpenAPI | Swagger UI por serviço |
| **Métricas** | Micrometer + Prometheus | Coleta de métricas |
| **Tracing** | Micrometer Tracing + Zipkin | Rastreamento distribuído |
| **Resiliência** | Resilience4j | Circuit breaker nas chamadas síncronas |
| **Build** | Maven (multi-module) | Monorepo com módulos independentes |
| **Containers** | Docker + Docker Compose | Infraestrutura local e deploy |
| **CI/CD** | GitHub Actions | Build, test e lint automatizados |

---

## Serviços

| Serviço | Porta | Responsabilidade | Status |
|---------|-------|-----------------|--------|
| `api-gateway` | 8080 | Roteamento, autenticação JWT, rate limiting | 📋 Planejado |
| `catalog-service` | 8081 | CRUD de produtos, categorias, estoque, cache Redis | 🚧 Em progresso |
| `auth-service` | 8082 | Registro, login, refresh token, roles (ADMIN/CUSTOMER) | 📋 Planejado |
| `orders-service` | 8083 | Criação e ciclo de vida de pedidos, eventos Kafka | 📋 Planejado |
| `payments-service` | 8084 | Simulação de processamento de pagamentos | 📋 Planejado |
| `delivery-service` | 8085 | Rastreamento de entregas | 📋 Planejado |
| `notifications-service` | 8086 | Envio de notificações via email/push (Kafka consumer) | 📋 Planejado |
| `analytics-service` | 8087 | Consumo de eventos para relatórios (MongoDB) | 📋 Planejado |

---

## catalog-service — API Reference

Base URL: `http://localhost:8081/api/v1`

| Método | Endpoint | Descrição | Status HTTP |
|--------|----------|-----------|------------|
| `POST` | `/products` | Cadastrar produto | 201 Created |
| `GET` | `/products` | Listar produtos (paginado) | 200 OK |
| `GET` | `/products/{id}` | Buscar produto por ID | 200 OK |
| `PUT` | `/products/{id}` | Atualizar produto (partial update) | 200 OK |
| `DELETE` | `/products/{id}` | Remover produto (soft delete) | 204 No Content |
| `PATCH` | `/products/{id}` | Restaurar produto deletado | 204 No Content |

**Paginação:** `GET /products?page=0&size=20&sort=id,asc`

**Exemplo de payload (POST /products):**
```json
{
  "name": "X-Burguer Clássico",
  "description": "Pão, carne 180g, queijo, alface e tomate",
  "price": 29.90,
  "category": "Lanches",
  "imageUrl": "https://cdn.foodhub.com/products/xburguer.jpg",
  "available": true,
  "stockQuantity": 50
}
```

---

## Como rodar localmente

### Pré-requisitos

- Java 17+
- Docker e Docker Compose
- Maven (ou use o wrapper `./mvnw`)

### 1. Subir a infraestrutura

```bash
docker compose up -d
```

Isso inicia PostgreSQL (5432) e Redis (6379).

### 2. Rodar o catalog-service

```bash
./mvnw spring-boot:run -pl catalog-service
```

O serviço estará disponível em `http://localhost:8081`.

### 3. Rodar os testes

```bash
# Requer Docker (Testcontainers)
./mvnw test -pl catalog-service
```

### 4. Build completo

```bash
./mvnw clean package -DskipTests
```

---

## Roadmap

### Fase 1 — catalog-service `🚧 Em progresso`

- [x] CRUD de produtos (POST, GET, PUT, DELETE)
- [x] Soft delete + restore
- [x] Paginação
- [x] Flyway migrations versionadas
- [x] Testes de integração com Testcontainers
- [ ] Respostas de erro estruturadas (`ErrorResponse` com campo, mensagem e timestamp)
- [ ] Validação detalhada: retornar erros por campo no 400
- [ ] Filtros de busca: `?category=`, `?available=`, `?minPrice=`, `?maxPrice=`, `?name=`
- [ ] OpenAPI / Swagger UI (`springdoc-openapi`)
- [ ] Cache Redis em `GET /products` e `GET /products/{id}`
- [ ] Spring Actuator + métricas Prometheus em `/actuator/prometheus`
- [ ] Testes de integração completos (todos os endpoints)

### Fase 2 — auth-service `📋 Planejado`

- [ ] Spring Security + JWT (access token 15 min, refresh token 7 dias)
- [ ] Roles: `ADMIN`, `CUSTOMER`
- [ ] Endpoints: `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`

### Fase 3 — orders-service `📋 Planejado`

- [ ] Entidades: `Order`, `OrderItem`
- [ ] FSM de status: `PENDING → CONFIRMED → PREPARING → READY → DELIVERING → DELIVERED`
- [ ] Publicação de eventos Kafka (`order.created`, `order.status.updated`)
- [ ] Outbox pattern para confiabilidade na publicação
- [ ] Validação de produto/estoque via OpenFeign → catalog-service
- [ ] Circuit breaker com Resilience4j

### Fase 4 — api-gateway `📋 Planejado`

- [ ] Spring Cloud Gateway
- [ ] Roteamento para todos os serviços
- [ ] Filtro global de autenticação JWT
- [ ] Rate limiting por IP

### Fase 5 — payments-service `📋 Planejado`

- [ ] Consumo de `order.created` via Kafka
- [ ] Simulação de aprovação/recusa de pagamento
- [ ] Publicação de `payment.processed`

### Fase 6 — notifications-service `📋 Planejado`

- [ ] Consumer Kafka para eventos de order e payment
- [ ] Envio de email simulado (extensível para AWS SES)

### Fase 7 — Observabilidade e CI/CD `📋 Planejado`

- [ ] Distributed tracing: Micrometer Tracing + Zipkin
- [ ] Dashboards Grafana para métricas de cada serviço
- [ ] GitHub Actions: build, test e análise de cobertura (JaCoCo)
- [ ] Docker Compose completo com toda a infraestrutura

---

## Estrutura do repositório

```
foodhub-backend/
├── catalog-service/        # Serviço de catálogo de produtos
│   ├── src/
│   │   ├── main/java/com/pereira/catalog/
│   │   │   ├── controller/   # REST controllers + GlobalExceptionHandler
│   │   │   ├── service/      # Interfaces + impl/
│   │   │   ├── repository/   # Spring Data JPA
│   │   │   ├── entity/       # JPA entities (soft delete)
│   │   │   └── dto/
│   │   │       ├── request/  # Records com Bean Validation
│   │   │       └── response/ # Records com factory .from(entity)
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/ # Flyway V1, V2, V3...
│   └── Dockerfile
├── docker-compose.yml
├── pom.xml                 # Parent POM (Spring Boot 3.5, Java 17)
└── README.md
```

---

## Contribuindo

Este é um projeto de portfólio pessoal, mas sugestões são bem-vindas via Issues.

---

## Licença

Distribuído sob a licença MIT. Veja `LICENSE` para mais informações.
