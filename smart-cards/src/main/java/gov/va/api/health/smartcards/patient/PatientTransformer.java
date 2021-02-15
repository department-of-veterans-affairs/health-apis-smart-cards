package gov.va.api.health.smartcards.patient;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;

@Builder
public class PatientTransformer {
  Patient patient;

  private List<Identifier> identifiers() {
    if (isEmpty(patient.identifier())) {
      return new ArrayList<>();
    }
    // Only return MPI Identifier
    return patient.identifier().stream()
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
    return patient.name().stream().map(this::name).collect(toList());
  }

  Patient transform() {
    /*
     * Do not include any id, meta, text, display elements
     */
    return Patient.builder()
        .resourceType("Patient")
        .id(patient.id())
        .identifier(identifiers())
        .name(names())
        .gender(patient.gender())
        .birthDate(patient.birthDate())
        .build();
  }
}
