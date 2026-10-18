
FROM  maven:3.9.9-eclipse-temurin-21  as build

WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre


RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*


WORKDIR /app

COPY --from=build  /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

