## Stage Build
#FROM maven:3.9.11 AS build
#WORKDIR /app
#
#COPY pom.xml .
#COPY src ./src
#RUN mvn clean package -DskipTests
#
## Stage runtime
#FROM eclipse-temurin:17-jdk-alpine
#
#WORKDIR /app
#
#RUN apk add --no-cache curl
#
#COPY --from=build /app/target/*.jar app.jar
#
#EXPOSE 8761
#
#ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-jar", "/app/app.jar"]


FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-jar", "app.jar"]