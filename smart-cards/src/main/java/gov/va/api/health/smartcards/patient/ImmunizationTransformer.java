package gov.va.api.health.smartcards.patient;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
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

  String occurrenceDateTime() {
    // only publish the date information. time is not necessary.
    String odt = entry.resource().occurrenceDateTime();
    // expect DQ to provide a ISO8601-formatted date
    if (odt.contains("T")) {
      odt = Iterables.get(Splitter.on('T').split(odt), 0);
    }
    return odt;
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
                .occurrenceDateTime(occurrenceDateTime())
                .location(location())
                .build())
        .build();
  }

  CodeableConcept vaccineCode() {
    // Rebuild object with Coding only
    return CodeableConcept.builder().coding(entry.resource().vaccineCode().coding()).build();
  }
}
