package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.Patient;

public interface FhirClient {
  Immunization.Bundle immunizationBundle(String icn, String authorization);

  Location.Bundle locationBundle(String id, String authorization);

  Patient.Bundle patientBundle(String icn, String authorization);
}
