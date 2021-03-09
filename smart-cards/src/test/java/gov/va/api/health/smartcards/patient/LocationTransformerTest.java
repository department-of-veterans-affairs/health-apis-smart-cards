package gov.va.api.health.smartcards.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.Location.Mode;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LocationTransformerTest {

  @Test
  public void basic() {
    var location = location();
    assertThat(LocationTransformer.builder().entry(location).build().transform())
        .isEqualTo(
            MixedEntry.builder()
                .fullUrl("http://example.com/r4/Location/loc-1")
                .resource(
                    Location.builder()
                        .resourceType("Location")
                        .name("Location with id loc-1")
                        .address(
                            Address.builder()
                                .text("1901 VETERANS MEMORIAL DRIVE TEMPLE TEXAS 76504")
                                .line(List.of("1901 VETERANS MEMORIAL DRIVE"))
                                .city("TEMPLE")
                                .state("TEXAS")
                                .postalCode("76504")
                                .build())
                        .build())
                .build());
  }

  private Location.Entry location() {
    return Location.Entry.builder()
        .fullUrl("http://example.com/r4/Location/loc-1")
        .resource(
            Location.builder()
                .id("loc-1")
                .status(Location.Status.active)
                .name("Location with id loc-1")
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
                .build())
        .build();
  }
}
