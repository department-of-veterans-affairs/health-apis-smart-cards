# health-apis-smart-cards

A [Spring Boot](https://spring.io/projects/spring-boot) application that issues
[verifiable health cards](https://w3c.github.io/vc-data-model)
in accordance with the [SMART Health Cards Framework](https://smarthealth.cards).
The underlying data is provided by the [Data Query](https://github.com/department-of-veterans-affairs/health-apis-data-query) API.

## System Components

![components](src/plantuml/components.png)

The API Gateway handles authentication and rate limiting before traffic is received by Smart Cards.

- **Kong** is responsible for token validation and SMART-on-FHIR OAuth scope enforcement
- **Smart Cards** processes consumer requests for the `/Patient/<<id>>/$health-cards-issue` endpoint
- **Data Query** provides FHIR-compliant health data for the Patient, enforces additional authorization,
  and handles processing of private data
- **Datamart** is a database within the Corporate Data Warehouse that provides
  read-only data for Data Query (originating from VistA)

## Modules

- [pem-to-jwk](pem-to-jwk/README.md) - Simple utility to convert PEM-encoded keys to [JWK](https://tools.ietf.org/html/rfc7517)
- [smart-cards](smart-cards/README.md) - Main API implementation
- [smart-cards-mock-services](smart-cards-mock-services/README.md) - Mock FHIR server to support local development and integration tests
- [smart-cards-tests](smart-cards-tests/README.md) - Integration tests

## Local Development

Refer to [health-apis-parent](https://github.com/department-of-veterans-affairs/health-apis-parent)
for basic environment setup. (Java, Maven, Docker, etc.)
Execute `mvn clean install` to build all of the modules, then follow the local development
instructions for [mock-services](smart-cards-mock-services/README.md#local-development)
and [smart-cards](smart-cards/README.md#local-development).
