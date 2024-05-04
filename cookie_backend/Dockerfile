FROM maven:3.8.1-openjdk-17-slim AS build
WORKDIR /usr/local/app

COPY src ./src
COPY pom.xml .
RUN mvn clean package

#SECOND STAGE
FROM eclipse-temurin:17-jre-jammy
WORKDIR /usr/local/bin

COPY --from=build /usr/local/app/target/*.jar ./cookie_app.jar

EXPOSE 8080
CMD ["java", "-jar", "-Dspring.profiles.active=test", "/usr/local/bin/cookie_app.jar"]
