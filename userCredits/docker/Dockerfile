FROM maven:3-amazoncorretto-21-alpine as build
WORKDIR /app

COPY pom.xml .
COPY ./src/ ./src/

RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]