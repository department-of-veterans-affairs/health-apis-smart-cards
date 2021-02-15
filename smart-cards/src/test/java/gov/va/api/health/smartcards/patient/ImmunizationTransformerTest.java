package gov.va.api.health.smartcards.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.Annotation;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Immunization.Status;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ImmunizationTransformerTest {
  @Test
  public void basic() {
    var immunization = immunization();
    assertThat(ImmunizationTransformer.builder().immunization(immunization).build().transform())
        .isEqualTo(
            Immunization.builder()
                .resourceType("Immunization")
                .id("imm-1")
                .status(Status.completed)
                .vaccineCode(
                    CodeableConcept.builder()
                        .coding(
                            List.of(
                                Coding.builder()
                                    .system("http://hl7.org/fhir/sid/cvx")
                                    .code("207")
                                    .build()))
                        .build())
                .patient(Reference.builder().reference("https://foo.com/r4/Patient/x").build())
                .occurrenceDateTime("2020-12-18T12:24:55Z")
                .primarySource(true)
                .location(
                    Reference.builder().reference("https://foo.com/r4/Location/loc-1").build())
                .build());
  }

  private Immunization immunization() {
    return Immunization.builder()
        .resourceType("Immunization")
        .id("imm-1")
        .status(Status.completed)
        .vaccineCode(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder().system("http://hl7.org/fhir/sid/cvx").code("207").build()))
                .text("COVID-19, mRNA, LNP-S, PF, 100 mcg/ 0.5 mL dose")
                .build())
        .patient(
            Reference.builder()
                .reference("https://foo.com/r4/Patient/x")
                .display("Joe Doe")
                .build())
        .occurrenceDateTime("2020-12-18T12:24:55Z")
        .primarySource(true)
        .location(
            Reference.builder()
                .reference("https://foo.com/r4/Location/loc-1")
                .display("LOC1 VAMC")
                .build())
        .note(
            List.of(
                Annotation.builder()
                    .text(
                        "Dose #1 of 2 of COVID-19, mRNA, LNP-S, PF, 100 mcg/ 0.5 mL dose "
                            + "vaccine administered.")
                    .build()))
        .build();
  }
}
