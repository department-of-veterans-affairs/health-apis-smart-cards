package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.resources.Patient;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public final class OnStartupConfig {
  private static final String PATIENT_IDENTIFIER_SIZE_PROPERTY =
      Patient.class.getName() + ".identifier.min-size";

  @PostConstruct
  void init() {
    Patient.IDENTIFIER_MIN_SIZE.set(0);
    log.info("{} set to 0", PATIENT_IDENTIFIER_SIZE_PROPERTY);
  }
}
