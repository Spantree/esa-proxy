ESA Elasticsearch Endpoint
--------------------------

Proxies requests to Elasticsearch. It replicates the http API, but adds an authorization layer.
Each request can include an __apiToken__ field that the proxy can use to determine what sort of
access to grant to the user.

## Starting the server
```bash
./gradlew run
```

This will start the server on port 5051.

## Running tests
Tests can be run in Intellij, or via the command line:

```bash
./gradlew test
```

## Contributing
1. Every new feature must include a test. 
2. Every new file must also include the Apache License at the beginning of the file.
3. Add at least a sentence describing what the class that is created does.
