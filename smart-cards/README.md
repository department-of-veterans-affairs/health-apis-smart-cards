# smart-cards

Main application.

## Local Development

### Config
`../make-configs.sh`

Use `less config/application-dev.properties` to verify application properties for local development.

### Build

Full build:

`mvn clean install`

To build without additional formatting, code coverage enforcement, static code analysis, integration tests, etc., disable the `standard` profile:

`mvn -P'!standard' package`

Start application:

`java -Dspring.profiles.active=dev -jar target/smart-cards-${VERSION}.jar`

## Design

This module uses the [Spring Boot](https://spring.io/projects/spring-boot) framework and follows its conventions.

### Internal Components

![classes](../src/plantuml/controller-design.png)

### Generating a Health Card

![sequence](../src/plantuml/controller-sequence.png)

### Components

- `WebExceptionHandler` - Common error handling and `OperationOutcome` generation with appropriate HTTP status codes
- `PatientController` - Spring Rest Controller responsible for processing the request; it orchestrates the FHIR API lookups, data minimization and transformation, VC object generation, payload signing to JWS, and final Parameters response generation
- `DataQueryFhirClient` - FHIR HTTP client for invoking Data Query and filtering results
- `{Resource}Minimizer` - Minimize the amount of data to be embedded in the Health Card
- `PayloadSigner` - Sign payload to generate a JWS
