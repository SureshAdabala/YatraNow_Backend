# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2: Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from Stage 1
COPY --from=build /app/target/yatranow-backend-1.0.0.jar app.jar

# Render injects PORT; fallback to 9090 for local runs
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
