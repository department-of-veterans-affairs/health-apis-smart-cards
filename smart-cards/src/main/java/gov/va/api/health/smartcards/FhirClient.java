package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.Optional;

public interface FhirClient {
  Immunization.Bundle immunizationBundle(Patient patient);

  Optional<Patient> patient(String id);
}
