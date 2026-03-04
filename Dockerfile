#OpenJDK 17 image
FROM eclipse-temurin:21-jdk-alpine

ARG JAR_FILE=target/taskmaster-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]