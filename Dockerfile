FROM openjdk:13-jdk-alpine

COPY target/RestShaper-0.1-SNAPSHOT-jar-with-dependencies.jar  target/RestShaper-0.1-SNAPSHOT-jar-with-dependencies.jar
COPY test-config.json test-config.json


CMD ["java", "-jar", "target/RestShaper-0.1-SNAPSHOT-jar-with-dependencies.jar", "test-config.json"]