package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.ContactDetail;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

public class MetadataR4ControllerTest {
  @Test
  void read() {
    Properties properties = new Properties();
    properties.setProperty("group", "foo.bar");
    properties.setProperty("artifact", "smart-cards");
    properties.setProperty("version", "3.14159");
    properties.setProperty("time", "2005-01-21T07:57:00Z");
    BuildProperties buildProperties = new BuildProperties(properties);
    assertThat(
            new MetadataR4Controller(
                    buildProperties,
                    LinkProperties.builder()
                        .baseUrl("http://va.gov")
                        .r4BasePath("api/r4")
                        .dqInternalR4BasePath("/r4")
                        .dqInternalUrl("/fhir/v0/r4")
                        .build())
                .read())
        .isEqualTo(
            CapabilityStatement.builder()
                .id("smart-cards-capability-statement")
                .resourceType("CapabilityStatement")
                .version("3.14159")
                .name("API Management Platform | Smart Cards - R4")
                .title("API Management Platform | Smart Cards - R4")
                .status(CapabilityStatement.Status.active)
                .experimental(true)
                .date("2005-01-21T07:57:00Z")
                .publisher("Department of Veterans Affairs")
                .contact(
                    List.of(
                        ContactDetail.builder()
                            .name("API Support")
                            .telecom(
                                List.of(
                                    ContactPoint.builder()
                                        .system(ContactPoint.ContactPointSystem.email)
                                        .value("api@va.gov")
                                        .build()))
                            .build()))
                .description("Read and search support for smart health cards.")
                .kind(CapabilityStatement.Kind.capability)
                .software(
                    CapabilityStatement.Software.builder()
                        .name("foo.bar:smart-cards")
                        .version("3.14159")
                        .releaseDate("2005-01-21T07:57:00Z")
                        .build())
                .implementation(
                    CapabilityStatement.Implementation.builder()
                        .description("API Management Platform | Smart Cards - R4")
                        .url("http://va.gov/api/r4")
                        .build())
                .fhirVersion("4.0.1")
                .format(List.of("application/json", "application/fhir+json"))
                .rest(
                    List.of(
                        CapabilityStatement.Rest.builder()
                            .mode(CapabilityStatement.RestMode.server)
                            .resource(
                                List.of(
                                    CapabilityStatement.CapabilityResource.builder()
                                        .type("Parameters")
                                        .profile("https://www.hl7.org/fhir/r4/parameters.html")
                                        .interaction(
                                            List.of(
                                                CapabilityStatement.ResourceInteraction.builder()
                                                    .code(
                                                        CapabilityStatement.TypeRestfulInteraction
                                                            .read)
                                                    .documentation(
                                                        "Implemented per specification. See http://hl7.org/fhir/R4/http.html")
                                                    .build()))
                                        .versioning(CapabilityStatement.Versioning.no_version)
                                        .referencePolicy(
                                            List.of(
                                                CapabilityStatement.ReferencePolicy.literal,
                                                CapabilityStatement.ReferencePolicy.local))
                                        .build()))
                            .build()))
                .build());
  }
}
