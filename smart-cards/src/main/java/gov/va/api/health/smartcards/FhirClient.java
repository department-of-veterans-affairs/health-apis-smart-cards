package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;

public interface FhirClient {
  Immunization.Bundle immunizationBundle(Patient patient);

  Patient.Bundle patientBundle(String id, String authorization);
}
