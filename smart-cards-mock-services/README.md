# smart-cards-mock-services

Mock Service application, used for local testing.

## Local Development

### Build

To run full build:

`mvn clean install`

To run build without additional formatting, code coverage enforcement, static code analysis, integration tests, etc., disable the `standard` profile:

`mvn -P'!standard' package`

Start Java app:

`java -Dspring.profiles.active=dev -jar target/smart-cards-${VERSION}.jar`
