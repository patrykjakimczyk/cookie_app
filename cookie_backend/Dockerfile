FROM eclipse-temurin:17-jdk-jammy

WORKDIR /home/cookie_app

COPY src ./src
COPY .mvn/ ./.mvn
COPY mvnw pom.xml .

RUN ./mvnw clean package
RUN cp ./target/*.jar /usr/local/bin/cookie_app.jar

EXPOSE 8080
CMD ["java", "-jar", "-Dspring.profiles.active=docker", "/usr/local/bin/cookie_app.jar"]
