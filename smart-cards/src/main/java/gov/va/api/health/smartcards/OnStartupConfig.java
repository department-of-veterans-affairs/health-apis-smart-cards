package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.resources.Patient;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OnStartupConfig {

  @Value("${gov.va.api.health.r4.api.resources.Patient.identifier.min-size:#{null}}")
  private String identifierMinSize;

  @PostConstruct
  void init() {
    overridePatientIdentifierMinSize();
  }

  private void overridePatientIdentifierMinSize() {
    var currentIdentifierMinSize = Patient.IDENTIFIER_MIN_SIZE.get();
    log.info("R4 Patient resource identifier.min-size is {}", currentIdentifierMinSize);

    // JVM Property takes precedence.
    // Property is nullable, so only attempt override if explicitly set.
    // Otherwise, continue with default value.
    if (System.getProperty(Patient.class.getName() + ".identifier.min-size") == null
        && identifierMinSize != null) {
      log.info(
          "R4 Patient resource identifier.min-size: overriding from {} to property value {}",
          currentIdentifierMinSize,
          identifierMinSize);
      Patient.IDENTIFIER_MIN_SIZE.set(Integer.parseInt(identifierMinSize));
    }
  }
}
