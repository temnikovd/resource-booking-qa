# =========================
# Build stage
# =========================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml ./
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B package -DskipTests

# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# =========================
# Environment variables
# =========================
ENV QA_TEST_ADMIN_CREATION_SECRET=""
ENV JWT_SECRET=""
ENV JWT_EXPIRATION_SECONDS="3600"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
