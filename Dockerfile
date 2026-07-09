FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copy the inner Task_Manager directory contents
COPY Task_Manager/pom.xml .
COPY Task_Manager/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/Task_Manager-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
