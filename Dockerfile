FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy only the built jar
COPY target/*.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
