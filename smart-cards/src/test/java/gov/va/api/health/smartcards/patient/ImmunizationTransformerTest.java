package gov.va.api.health.smartcards.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.Annotation;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.Location.Mode;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ImmunizationTransformerTest {
  @Test
  public void basic() {
    var immunization = immunization();
    assertThat(ImmunizationTransformer.builder().entry(immunization).build().transform())
        .isEqualTo(
            MixedEntry.builder()
                .fullUrl("http://example.com/r4/Immunization/imm-1")
                .resource(
                    Immunization.builder()
                        .resourceType("Immunization")
                        .status(Immunization.Status.completed)
                        .vaccineCode(
                            CodeableConcept.builder()
                                .coding(
                                    List.of(
                                        Coding.builder()
                                            .system("http://hl7.org/fhir/sid/cvx")
                                            .code("207")
                                            .build()))
                                .build())
                        .patient(
                            Reference.builder().reference("https://foo.com/r4/Patient/x").build())
                        .occurrenceDateTime("2020-12-18")
                        .performer(
                            List.of(
                                Immunization.Performer.builder()
                                    .actor(
                                        Reference.builder().display("MNG ORG VA MEDICAL").build())
                                    .build()))
                        .build())
                .build());
  }

  private Immunization.Entry immunization() {
    return Immunization.Entry.builder()
        .fullUrl("http://example.com/r4/Immunization/imm-1")
        .resource(
            Immunization.builder()
                .resourceType("Immunization")
                .id("imm-1")
                .status(Immunization.Status.completed)
                .vaccineCode(
                    CodeableConcept.builder()
                        .coding(
                            List.of(
                                Coding.builder()
                                    .system("http://hl7.org/fhir/sid/cvx")
                                    .code("207")
                                    .build()))
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
                .contained(List.of(location()))
                .build())
        .build();
  }

  private Location location() {
    return Location.builder()
        .id("loc-1")
        .status(Location.Status.active)
        .name("LOC1 LOCATION")
        .description("Description for id loc-id")
        .mode(Mode.instance)
        .type(
            List.of(
                CodeableConcept.builder()
                    .coding(List.of(Coding.builder().display("SOME CODING").build()))
                    .text("SOME TEXT")
                    .build()))
        .telecom(
            List.of(
                ContactPoint.builder()
                    .system(ContactPointSystem.phone)
                    .value("123-456-7890 x0001")
                    .build()))
        .address(
            Address.builder()
                .text("1901 VETERANS MEMORIAL DRIVE TEMPLE TEXAS 76504")
                .line(List.of("1901 VETERANS MEMORIAL DRIVE"))
                .city("TEMPLE")
                .state("TEXAS")
                .postalCode("76504")
                .build())
        .physicalType(
            CodeableConcept.builder()
                .coding(List.of(Coding.builder().display("PHYS TYPE CODING").build()))
                .text("PHYS TYPE TEXT")
                .build())
        .managingOrganization(
            Reference.builder()
                .reference("http://example.com/r4/Organization/org-for-loc-1")
                .display("MNG ORG VA MEDICAL")
                .build())
        .build();
  }
}
