package gov.va.api.health.smartcards.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.List;
import javax.validation.Validation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PatientMinimizerTest {
  @BeforeAll
  static void init() {
    Patient.IDENTIFIER_MIN_SIZE.set(0);
  }

  static Identifier mpi(String id) {
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

  static Patient.Entry patient() {
    String firstName = "Joe";
    String lastName = "Doe";
    return Patient.Entry.builder()
        .fullUrl("http://example.com/r4/Patient/x")
        .resource(
            Patient.builder()
                .resourceType("Patient")
                .id("x")
                .identifier(
                    List.of(
                        Identifier.builder().use(Identifier.IdentifierUse.temp).value("x").build(),
                        mpi("x")))
                .active(true)
                .name(
                    List.of(
                        HumanName.builder()
                            .use(HumanName.NameUse.anonymous)
                            .text(String.format("%s %s", firstName, lastName))
                            .family(lastName)
                            .given(List.of(firstName))
                            .build()))
                .gender(Patient.Gender.unknown)
                .birthDate("1955-01-01")
                .deceasedBoolean(false)
                .build())
        .build();
  }

  @Test
  void basic() {
    var patient = patient();
    var transformed = PatientMinimizer.builder().entry(patient).build().transform();
    assertThat(transformed)
        .isEqualTo(
            MixedEntry.builder()
                .fullUrl("http://example.com/r4/Patient/x")
                .resource(
                    Patient.builder()
                        .resourceType("Patient")
                        .name(
                            List.of(
                                HumanName.builder().family("Doe").given(List.of("Joe")).build()))
                        .gender(Patient.Gender.unknown)
                        .birthDate("1955-01-01")
                        .build())
                .build());
    assertThat(Validation.buildDefaultValidatorFactory().getValidator().validate(transformed))
        .isEmpty();
  }
}
