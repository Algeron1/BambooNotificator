FROM eclipse-temurin:24-jdk-alpine

WORKDIR /app
COPY target/*.jar app.jar
COPY src/main/resources/application-docker.yml /app/config/

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar", \
           "--spring.config.location=optional:classpath:/,optional:classpath:/config/,optional:file:/app/config/", \
           "--spring.data.redis.host=redis", \
           "--spring.data.redis.port=6379"]