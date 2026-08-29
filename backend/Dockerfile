FROM maven:3.9-eclipse-temurin-21 AS build
LABEL org.opencontainers.image.source=https://github.com/Cloud-Native-Engeneering/Cloud-Native-Engineering-project-group2
WORKDIR /app
COPY . .
RUN mvn package -DskipTests && cp target/*.jar app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
