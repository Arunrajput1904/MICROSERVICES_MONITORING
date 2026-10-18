
FROM  maven:3.9.9-eclipse-temurin-21  as build

WORKDIR /app

COPY pom.xml ./
COPY src ./src

Run mvn clean package -DskipTests

From eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build  /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

