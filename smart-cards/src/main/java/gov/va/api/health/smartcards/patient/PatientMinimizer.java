package gov.va.api.health.smartcards.patient;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;

@Builder
public final class PatientMinimizer {
  @NonNull private final Patient.Entry entry;

  private static HumanName name(HumanName name) {
    return HumanName.builder().family(name.family()).given(name.given()).build();
  }

  MixedEntry minimize() {
    // do not include any id, meta, text, display elements
    return MixedEntry.builder()
        .fullUrl(entry.fullUrl())
        .resource(
            Patient.builder()
                .name(names())
                .gender(entry.resource().gender())
                .birthDate(entry.resource().birthDate())
                .build())
        .build();
  }

  private List<HumanName> names() {
    return entry.resource().name().stream().map(PatientMinimizer::name).collect(toList());
  }
}
