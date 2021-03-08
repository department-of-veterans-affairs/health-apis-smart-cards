package gov.va.api.health.smartcards.patient;

import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Immunization;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class ImmunizationTransformer {
  Immunization.Entry entry;

  static Reference referenceOnly(@NonNull Reference reference) {
    return Reference.builder().reference(reference.reference()).build();
  }

  Reference location() {
    if (entry.resource().location() == null) {
      return null;
    }
    return referenceOnly(entry.resource().location());
  }

  Reference patient() {
    // Do not include display
    return referenceOnly(entry.resource().patient());
  }

  MixedEntry transform() {
    return MixedEntry.builder()
        .fullUrl(entry.fullUrl())
        .resource(
            Immunization.builder()
                .status(entry.resource().status())
                .vaccineCode(vaccineCode())
                .patient(patient())
                .occurrenceDateTime(entry.resource().occurrenceDateTime())
                .location(location())
                .build())
        .search(AbstractEntry.Search.builder().mode(AbstractEntry.SearchMode.match).build())
        .build();
  }

  CodeableConcept vaccineCode() {
    // Rebuild object with Coding only
    return CodeableConcept.builder().coding(entry.resource().vaccineCode().coding()).build();
  }
}
