package gov.va.api.health.smartcards.patient;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Immunization;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class ImmunizationTransformer {
  Immunization immunization;

  static Reference referenceOnly(@NonNull Reference reference) {
    return Reference.builder().reference(reference.reference()).build();
  }

  Reference location() {
    if (immunization.location() == null) {
      return null;
    }
    return referenceOnly(immunization.location());
  }

  Reference patient() {
    // Do not include display
    return referenceOnly(immunization.patient());
  }

  Immunization transform() {
    return Immunization.builder()
        .resourceType("Immunization")
        .status(immunization.status())
        .vaccineCode(vaccineCode())
        .patient(patient())
        .occurrenceDateTime(immunization.occurrenceDateTime())
        .primarySource(immunization.primarySource())
        .location(location())
        .build();
  }

  CodeableConcept vaccineCode() {
    // Rebuild object with Coding only
    return CodeableConcept.builder().coding(immunization.vaccineCode().coding()).build();
  }
}
