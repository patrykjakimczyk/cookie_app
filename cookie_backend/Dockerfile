FROM maven:3.8.1-openjdk-17-slim AS build
WORKDIR /usr/local/app

COPY src ./src
#COPY .mvn/ ./.mvn
COPY pom.xml .
#COPY mvnw .
RUN mvn clean package
#COPY mvnw pom.xml ./
#COPY ./src ./src
#RUN ./mvnw clean package

#SECOND STAGE
FROM eclipse-temurin:17-jre-jammy
WORKDIR /usr/local/bin

COPY --from=build /usr/local/app/target/*.jar ./cookie_app.jar

EXPOSE 8080
CMD ["java", "-jar", "-Dspring.profiles.active=test", "/usr/local/bin/cookie_app.jar"]
