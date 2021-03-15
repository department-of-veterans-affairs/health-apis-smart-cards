package gov.va.api.health.smartcards.patient;

import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Location;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Builder
public class ImmunizationTransformer {
  Immunization.Entry entry;

  static Reference referenceOnly(@NonNull Reference reference) {
    return Reference.builder().reference(reference.reference()).build();
  }

  String occurrenceDateTime() {
    // only publish the date information. time is not necessary.
    String odt = entry.resource().occurrenceDateTime();
    // expect DQ to provide a ISO8601-formatted date
    if (odt.contains("T")) {
      odt = odt.substring(0, odt.indexOf("T"));
    }
    return odt;
  }

  Reference patient() {
    // Do not include display
    return referenceOnly(entry.resource().patient());
  }

  List<Immunization.Performer> performer() {
    if (CollectionUtils.isEmpty(entry.resource().contained())) {
      return null;
    }
    Location location =
        entry.resource().contained().stream()
            .filter(r -> r instanceof Location)
            .map(r -> (Location) r)
            .findFirst()
            .orElse(null);
    if (location == null) {
      return null;
    }
    Reference org = location.managingOrganization();
    if (org == null || StringUtils.isEmpty(org.display())) {
      return null;
    }
    return List.of(
        Immunization.Performer.builder()
            .actor(Reference.builder().display(org.display()).build())
            .build());
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
                .performer(performer())
                .build())
        .build();
  }

  CodeableConcept vaccineCode() {
    // Rebuild object with Coding only
    return CodeableConcept.builder().coding(entry.resource().vaccineCode().coding()).build();
  }
}
