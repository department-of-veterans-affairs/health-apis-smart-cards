# smart-cards

Main application.

## Local Development

### Configs
Run `make-configs.sh` first (outputs properties to `config` directory):

`./make-configs.sh`

### Build

To run full build:

`maven clean install`

To run build without additional formatting, code coverage enforcement, static code analysis, integration tests, etc., disable the `standard` profile:

`mvn -P'!standard' package`

Start Java app:

`java -Dspring.profiles.active=dev -jar target/smart-cards-${VERSION}.jar`
