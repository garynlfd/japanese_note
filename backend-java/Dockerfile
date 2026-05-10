# stage 1(build): JDK + Maven + source code -> produce .jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn package -DskipTests
# stage 2(run): JRE only + .jar -> small, clean image
FROM eclipse-temurin:21-jre
COPY --from=build /app/target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]