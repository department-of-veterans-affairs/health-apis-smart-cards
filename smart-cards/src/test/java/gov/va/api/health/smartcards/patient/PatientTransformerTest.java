package gov.va.api.health.smartcards.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.bundle.AbstractEntry.Search;
import gov.va.api.health.r4.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.r4.api.bundle.MixedEntry;
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
    assertThat(PatientTransformer.builder().entry(patient).build().transform())
        .isEqualTo(
            MixedEntry.builder()
                .fullUrl("http://example.com/r4/Patient/x")
                .resource(
                    Patient.builder()
                        .resourceType("Patient")
                        .identifier(List.of(mpi("x")))
                        .name(
                            List.of(
                                HumanName.builder()
                                    .use(NameUse.anonymous)
                                    .family("Doe")
                                    .given(List.of("Joe"))
                                    .build()))
                        .gender(Gender.unknown)
                        .birthDate("1955-01-01")
                        .build())
                .search(Search.builder().mode(SearchMode.match).build())
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

  Patient.Entry patient() {
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
                        Identifier.builder().use(IdentifierUse.temp).value("x").build(), mpi("x")))
                .active(true)
                .name(
                    List.of(
                        HumanName.builder()
                            .use(NameUse.anonymous)
                            .text(String.format("%s %s", firstName, lastName))
                            .family(lastName)
                            .given(List.of(firstName))
                            .build()))
                .gender(Gender.unknown)
                .birthDate("1955-01-01")
                .deceasedBoolean(false)
                .build())
        .build();
  }
}