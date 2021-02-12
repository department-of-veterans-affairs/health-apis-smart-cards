package gov.va.api.health.smartcards.patient;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.HumanName.NameUse;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Identifier.IdentifierUse;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Patient.Gender;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PatientTransformerTest {
  @Test
  public void basic() {
    var patient = patient();
    assertThat(PatientTransformer.builder().patient(patient).build().transform())
        .isEqualTo(
            Patient.builder()
                .resourceType("Patient")
                .identifier(List.of(mpi("x")))
                .name(
                    List.of(
                        HumanName.builder()
                            .use(NameUse.anonymous)
                            .family("Doe")
                            .given(singletonList("Joe"))
                            .build()))
                .gender(Gender.unknown)
                .birthDate("1955-01-01")
                .build());
  }

  Identifier mpi(String id) {
    return Identifier.builder()
        .use(Identifier.IdentifierUse.usual)
        .type(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder().system("http://hl7.org/fhir/v2/0203").code("MR").build()))
                .build())
        .system("http://va.gov/mpi")
        .value(id)
        .assigner(Reference.builder().display("Master Patient Index").build())
        .build();
  }

  Patient patient() {
    String firstName = "Joe";
    String lastName = "Doe";
    return Patient.builder()
        .resourceType("Patient")
        .id("x")
        .identifier(
            List.of(Identifier.builder().use(IdentifierUse.temp).value("x").build(), mpi("x")))
        .active(true)
        .name(
            singletonList(
                HumanName.builder()
                    .use(NameUse.anonymous)
                    .text(String.format("%s %s", firstName, lastName))
                    .family(lastName)
                    .given(singletonList(firstName))
                    .build()))
        .gender(Gender.unknown)
        .birthDate("1955-01-01")
        .deceasedBoolean(false)
        .build();
  }
}
