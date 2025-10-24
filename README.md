# FoodHub Backend

Microserviços em Spring Boot para uma plataforma de pedidos e entregas (FoodHub).

- Stack: Spring Boot, Spring Data JPA, PostgreSQL, Kafka, RabbitMQ, Redis, AWS (S3/SNS/SQS)
- Qualidade: JUnit 5, Testcontainers, JaCoCo, Sonar
- Observabilidade: Micrometer/Prometheus, OpenTelemetry
- CI/CD: Jenkins, Docker, Docker Compose, Helm (futuro)

## Roadmap (resumo)
- [ ] catalog-service (CRUD produtos com Postgres + Flyway)
- [ ] orders-service (eventos Kafka)
- [ ] payments-service (simulação)
- [ ] delivery-service
- [ ] notifications-service (Rabbit/SNS/SQS)
- [ ] analytics-service (Mongo)
- [ ] API Gateway
