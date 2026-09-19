# Uso: docker build --build-arg MODULE=ms-pedidos360-orders .
FROM maven:3.9-eclipse-temurin-21-alpine AS build
ARG MODULE
WORKDIR /src
COPY . .
RUN mvn -q -B -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
ARG MODULE
COPY --from=build /src/${MODULE}/target/*.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
