FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY catalog-service/pom.xml catalog-service/pom.xml
RUN mvn dependency:go-offline -pl catalog-service -am -q
COPY catalog-service/src catalog-service/src
RUN mvn clean package -pl catalog-service -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/catalog-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
