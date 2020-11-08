# RestShaper

## Summary

RestShaper is a project designed to generate API test cases off of swagger documents. 
The difference with RestShaper is that it seeks to learn the... well,
 shape of the REST API under test, and alert on changes to that shape. R
 ather than generating cases purely at random, it aims to 
 * give the user some "steering" for the test case generation
 * inform future test case generation with the results of previous tests
 
 ## Getting started
 
 RestShaper, at it's core, takes an API documentation, 
 and calls into that API with semi-randomized input 
 that matches the shape of the API docs. 
 
 ### To Run with docker image or from the JAR.
 
 First you need a json config file. 
 See test/kotlin/Resources/TestConfig/PetTest.json for an example file. 
 It's argument match the configuration in [The configuration in kotlin](#configuration-in-kotlin), 
 except it does not include the generator functions. When using this run method 
 you always use the default input/output generators.
 
 The default generators take a swagger spec and output to a global variable or Json file. 
 You just need to define:
 * the number of TOTAL cases to run. <numCases>
 * the location of the swagger documentation(either a url or filepath). <swagger url>
 * The list of endpoints to test. Defined by their methods and paths. <endpoints>
 
 ## Configuration in Kotlin
 
 You can use the RestShaper Kotlin DSL to configure and run a test with one call! You can see examples in SimpleRunner.kt
 ``` fun petStoreGetOrder(numCases: Int = 5): Boolean {
        return runnerConfig {
            inputFunction = SimpleInputGenerator(numCases)::getInput  // You can add custom test gerators
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
 
 * Query parameter, path parameter, header generation
    * Currently, String, Integer, Decimal data types are supported.
 * Serialized results output in JSON
 
 ## Coming soon
 * request body generation
 * additional parameter data types
 * API call "chaining"
 * persistent result storage
 
 ## vocab and big picture tidbits
 - a "spec" IS A specification of how to generate values for test 
 inputs for a piece of the request under test.
 - 
