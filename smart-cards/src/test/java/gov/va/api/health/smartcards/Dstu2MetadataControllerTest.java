package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Conformance;
import gov.va.api.health.dstu2.api.resources.Conformance.AcceptUnknown;
import gov.va.api.health.dstu2.api.resources.Conformance.ResourceInteractionCode;
import gov.va.api.health.dstu2.api.resources.Conformance.RestResource;
import gov.va.api.health.dstu2.api.resources.Conformance.RestResourceVersion;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

public class Dstu2MetadataControllerTest {

  @Test
  void read() {
    Properties properties = new Properties();
    properties.setProperty("group", "foo.bar");
    properties.setProperty("artifact", "smart-cards");
    properties.setProperty("version", "3.14159");
    properties.setProperty("time", "2005-01-21T07:57:00Z");
    BuildProperties buildProperties = new BuildProperties(properties);
    assertThat(new Dstu2MetadataController(buildProperties).read())
        .isEqualTo(
            Conformance.builder()
                .id("smart-cards-conformance")
                .resourceType("Conformance")
                .version("1.0.2")
                .name("API Management Platform | Smart Cards - R4")
                .date("2005-01-21T07:57:00Z")
                .publisher("Department of Veterans Affairs")
                .contact(
                    List.of(
                        Conformance.Contact.builder()
                            .name("API Support")
                            .telecom(
                                List.of(
                                    gov.va.api.health.dstu2.api.datatypes.ContactPoint.builder()
                                        .system(
                                            gov.va.api.health.dstu2.api.datatypes.ContactPoint
                                                .ContactPointSystem.email)
                                        .value("api@va.gov")
                                        .build()))
                            .build()))
                .acceptUnknown(AcceptUnknown.no)
                .description("Read and search support for credentials of immunization by patient.")
                .kind(Conformance.Kind.capability)
                .software(
                    Conformance.Software.builder()
                        .name("foo.bar:smart-cards")
                        .version("3.14159")
                        .releaseDate("2005-01-21T07:57:00Z")
                        .build())
                .fhirVersion("4.0.1")
                .format(List.of("application/json", "application/fhir+json"))
                .rest(
                    List.of(
                        Conformance.Rest.builder()
                            .mode(Conformance.RestMode.server)
                            .resource(
                                List.of(
                                    RestResource.builder()
                                        .type("Parameters")
                                        .profile(
                                            Reference.builder()
                                                .reference(
                                                    "https://www.hl7.org/fhir/r4/parameters.html")
                                                .build())
                                        .interaction(
                                            List.of(
                                                resourceInteraction(ResourceInteractionCode.read)))
                                        .versioning(RestResourceVersion.no_version)
                                        .build()))
                            .build()))
                .build());
  }

  Conformance.ResourceInteraction resourceInteraction(Conformance.ResourceInteractionCode type) {
    return Conformance.ResourceInteraction.builder()
        .code(type)
        .documentation("Implemented per specification. See http://hl7.org/fhir/R4/http.html")
        .build();
  }
}
