FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /usr/local/app

COPY src ./src
COPY .mvn/ ./.mvn
COPY mvnw pom.xml ./
RUN ./mvnw clean package

#SECOND STAGE
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /usr/local/bin

COPY --from=build /usr/local/app/target/*.jar ./cookie_app.jar

EXPOSE 8080
CMD ["java", "-jar", "-Dspring.profiles.active=docker", "/usr/local/bin/cookie_app.jar"]
