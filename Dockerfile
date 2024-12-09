# Build stage
FROM maven:3.8-openjdk-17 AS build
COPY src /home/app/src

COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests=true

# Package stage
FROM openjdk:17-jdk-alpine
RUN apk update && apk upgrade && apk add ffmpeg
COPY --from=build /home/app/target/pulsestack-0.0.1-SNAPSHOT.jar /usr/local/lib/english.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/english.jar"]