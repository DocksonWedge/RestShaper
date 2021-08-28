FROM openjdk:13-jdk-alpine
COPY target/rest-shaper-api-jar-with-dependencies.jar  target/rest-shaper-api-jar-with-dependencies.jar

CMD ["java", "-jar", "target/rest-shaper-api-jar-with-dependencies.jar"]