# RestShaper

## Index

- [Summary](#summary)
- [Getting started](#getting-started)
- [To run with docker image or from the JAR](#to-run-with-docker-image-or-from-the-jar)
    - [from jar](#from-jar)
    - [from docker-compose](#from-docker-compose)
- [Configuration in Kotlin](#configuration-in-kotlin)
- [Currently supported](#currently-supported)
- [Coming soon](#coming-soon)
- [Vocab and big picture tidbits](#vocab-and-big-picture-tidbits)

## Summary

RestShaper is a project designed to generate API test cases off of swagger documents. The difference with RestShaper is
that it seeks to learn the... well, shape of the REST API under test, and alert on changes to that shape. R Rather than
generating cases purely at random, it aims to

* give the user some "steering" for the test case generation
* inform future test case generation with the results of previous tests

## Getting started

RestShaper, at it's core, takes an API documentation, and calls into that API with semi-randomized input that matches
the shape of the API docs.

### To run with docker image or from the JAR

First you need a json config file. See test/kotlin/Resources/TestConfig/PetTest.json for an example file. It's argument
match the configuration in [The configuration in kotlin](#configuration-in-kotlin), except it does not include the
generator functions. When using this run method you always use the default input/output generators.

The default generators take a swagger spec and output to a global variable or Json file. You just need to define:

* the number of TOTAL cases to run. => numCases
* the location of the swagger documentation(either a url or filepath). => swagger url
* The list of endpoints to test. Defined by their methods and paths. Leave empty to test all endpoints in the swagger
  spec. => endpoints

example so you don't have to go all the way over to example file. It's a long walk:

 ```
{
  "numCases": 5,
  "swaggerLocation": "https://petstore.swagger.io/v2/swagger.json",
  "endpoints": [
    {
      "method":  "POST",
      "path": "/pet"
    },
    {
      "method":  "GET",
      "path": "/pet/{petId}"
    }
  ]
}
   ```

### from jar

`mvn clean package` will build a jar in the `target` folder if you haven't downloaded it. You can then run the jar
with `java -jar target/RestShaper-[VERSION]-jar-with-dependencies.jar [Config-file-path]`
See compile-and-run.sh for an example.

### from docker-compose

Put your config in [test-config.json](test-config.json) in the source root. You can
copy [test-config.example.json](test-config.example.json) to start. Then, just run `docker-compose up`!

Remote docker image TBD.

### As an api with docker compose
`docker-compose -f docker-compose-api.yml up`

## Configuration in Kotlin

If you want a public kotlin interface, you can use the RestShaper Kotlin DSL to configure and run a test with one call!
You can see examples in SimpleRunner.kt

 ``` fun petStoreGetOrder(numCases: Int = 5): Boolean {
        return runnerConfig {
            inputFunction = SimpleInputGenerator(numCases)::getInput  // You can add custom test gerators
            // If there are values you want every request to have you can set them in staticParams
            staticParams = StaticParams( 
                headers = mapOf("api_key" to "token"),
                pathParams = mapOf("petId" to 12313213)
                queryParams = mapOf("param" to "some value")
                cookies = mapOf("session" to "some session info")
            )
            outputFunction = Results::saveToGlobal // You add a custom output function too
            endpointConfig = {
                swaggerUrl = "https://petstore.swagger.io/v2/swagger.json" // The url or file path of the swagger doc under test
                endpoints = listOf( // The list of all endpoints in swagger to test. 
                    GET to "/store/order/{orderId}"
                )
            }
        }.run()
    } 
```

## Currently supported

* Query parameter, path parameter, header, and request body generation
    * Currently, String, Integer, Decimal data types are supported.
* Serialized results output in JSON

## Coming soon

* additional parameter data types
* API call "chaining"
* persistent result storage

## Vocab and big picture tidbits

- a "spec" IS A specification of how to generate values for test inputs for a piece of the request under test.
- a "generator" IS A function that transforms information about the test
  (for example, an Endpoint spec or test results) to a new form for another area(for example, test query parameters or a
  results flat-file)

## Starting kafka

To run without Kafka set environment variable `USE_KAFKA` = false

`docker-compose -f kafka/docker-compose.kafka.yaml up -d --remove-orphans`

start with topics
`kafka-start.sh`

Docker desktop must be running

exec to container
`docker exec -it kafka bash`

create topic
`./usr/bin/kafka-topics --zookeeper zookeeper:2181 --create --topic my-first-topic --partitions 3 --if-not-exists`

list topics
`./usr/bin/kafka-topics --zookeeper zookeeper:2181 --list`

describe topic
`./usr/bin/kafka-topics --zookeeper zookeeper:2181 --topic result-store --describe`

produce message
`./usr/bin/kafka-console-producer --broker-list localhost:9092 --topic result-store --property parse.key=true --property key.separator=: < topic-input.txt`

consume message
`docker exec -i kafka ./usr/bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic result-body-store --property print.key=true --property key.separator=: --from-beginning`