FROM maven:3.9-eclipse-temurin-21
LABEL org.opencontainers.image.source=https://github.com/Cloud-Native-Engeneering/Cloud-Native-Engineering-project-group2
WORKDIR /app
COPY . .
RUN mvn package -DskipTests
EXPOSE 8080
CMD ["sh", "-c", "java -jar target/*.jar"]