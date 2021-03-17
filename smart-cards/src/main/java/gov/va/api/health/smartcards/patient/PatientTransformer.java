package gov.va.api.health.smartcards.patient;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class PatientTransformer {
  @NonNull Patient.Entry entry;

  private HumanName name(HumanName name) {
    return HumanName.builder().family(name.family()).given(name.given()).build();
  }

  private List<HumanName> names() {
    return entry.resource().name().stream().map(this::name).collect(toList());
  }

  MixedEntry transform() {
    /*
     * Do not include any id, meta, text, display elements
     */
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
}
