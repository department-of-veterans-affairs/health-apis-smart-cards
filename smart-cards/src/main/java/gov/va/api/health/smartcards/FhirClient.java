package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;

public interface FhirClient {
  Immunization.Bundle immunizationBundle(Patient patient, String authorization);

  Patient.Bundle patientBundle(String icn, String authorization);
}
