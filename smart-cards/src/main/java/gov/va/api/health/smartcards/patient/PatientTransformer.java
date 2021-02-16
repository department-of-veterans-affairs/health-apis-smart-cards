package gov.va.api.health.smartcards.patient;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.r4.api.bundle.AbstractEntry.Search;
import gov.va.api.health.r4.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;

@Builder
public class PatientTransformer {
  Patient.Entry entry;

  private List<Identifier> identifiers() {
    if (isEmpty(entry.resource().identifier())) {
      return new ArrayList<>();
    }
    // Only return MPI Identifier
    return entry.resource().identifier().stream()
        .filter(id -> "http://va.gov/mpi".equals(id.system()))
        .collect(toList());
  }

  private HumanName name(HumanName name) {
    return HumanName.builder()
        .use(name.use())
        .family(name.family())
        .given(name.given())
        .prefix(name.prefix())
        .suffix(name.suffix())
        .build();
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
                .resourceType("Patient")
                .identifier(identifiers())
                .name(names())
                .gender(entry.resource().gender())
                .birthDate(entry.resource().birthDate())
                .build())
        .search(Search.builder().mode(SearchMode.match).build())
        .build();
  }
}
