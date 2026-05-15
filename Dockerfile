# --- Stage 1: Build with Maven ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 9999
ENTRYPOINT ["java","-jar","app.jar"]
