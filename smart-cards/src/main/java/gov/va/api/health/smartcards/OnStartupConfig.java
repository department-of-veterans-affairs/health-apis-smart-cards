package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.resources.Patient;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OnStartupConfig {
  private static final String PATIENT_IDENTIFIER_SIZE_PROPERTY =
      Patient.class.getName() + ".identifier.min-size";

  @PostConstruct
  void init() {
    overridePatientIdentifierMinSize();
  }

  private void overridePatientIdentifierMinSize() {
    // JVM Property takes precedence
    if (System.getProperty(PATIENT_IDENTIFIER_SIZE_PROPERTY) == null) {
      Patient.IDENTIFIER_MIN_SIZE.set(0);
    }

    log.info(
        "R4 Patient resource identifier.min-size set to {}. Override with -D{}",
        Patient.IDENTIFIER_MIN_SIZE.get(),
        PATIENT_IDENTIFIER_SIZE_PROPERTY);
  }
}
