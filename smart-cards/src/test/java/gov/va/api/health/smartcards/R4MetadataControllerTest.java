package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.ContactDetail;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
import gov.va.api.health.r4.api.resources.CapabilityStatement.CapabilityResource;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Implementation;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Kind;
import gov.va.api.health.r4.api.resources.CapabilityStatement.ReferencePolicy;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Rest;
import gov.va.api.health.r4.api.resources.CapabilityStatement.RestMode;
import gov.va.api.health.r4.api.resources.CapabilityStatement.SearchParamType;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Software;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Status;
import gov.va.api.health.r4.api.resources.CapabilityStatement.TypeRestfulInteraction;
import gov.va.api.health.r4.api.resources.CapabilityStatement.Versioning;
import gov.va.api.health.smartcards.R4MetadataController.SearchParam;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

public class R4MetadataControllerTest {

  @Test
  void read() {
    Properties properties = new Properties();
    properties.setProperty("group", "foo.bar");
    properties.setProperty("artifact", "smart-cards");
    properties.setProperty("version", "3.14159");
    properties.setProperty("time", "2005-01-21T07:57:00Z");
    BuildProperties buildProperties = new BuildProperties(properties);
    assertThat(
            new R4MetadataController(
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
                .version("1.0.2")
                .name("API Management Platform | Smart Cards - R4")
                .title("API Management Platform | Smart Cards - R4")
                .status(Status.active)
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
                                        .system(ContactPointSystem.email)
                                        .value("api@va.gov")
                                        .build()))
                            .build()))
                .description("Read and search support for credentials of immunization by patient.")
                .kind(Kind.capability)
                .software(
                    Software.builder()
                        .name("foo.bar:smart-cards")
                        .version("3.14159")
                        .releaseDate("2005-01-21T07:57:00Z")
                        .build())
                .implementation(
                    Implementation.builder()
                        .description("API Management Platform | Smart Cards - R4")
                        .url("http://va.gov/api/r4")
                        .build())
                .fhirVersion("4.0.1")
                .format(List.of("application/json", "application/fhir+json"))
                .rest(
                    List.of(
                        Rest.builder()
                            .mode(RestMode.server)
                            .resource(
                                List.of(
                                    CapabilityResource.builder()
                                        .type("Patient")
                                        .profile("https://www.hl7.org/fhir/r4/patient.html")
                                        .interaction(
                                            List.of(
                                                resourceInteraction(TypeRestfulInteraction.read)))
                                        .versioning(Versioning.no_version)
                                        .referencePolicy(
                                            List.of(ReferencePolicy.literal, ReferencePolicy.local))
                                        .build()))
                            .build()))
                .build());
  }

  CapabilityStatement.ResourceInteraction resourceInteraction(
      CapabilityStatement.TypeRestfulInteraction type) {
    return CapabilityStatement.ResourceInteraction.builder()
        .code(type)
        .documentation("Implemented per specification. See http://hl7.org/fhir/R4/http.html")
        .build();
  }

  @Test
  void supportedResource() {
    assertThat(
            R4MetadataController.SupportedResource.builder()
                .type("type")
                .profileUrl("url")
                .searches(Set.of(SearchParam._ID))
                .build()
                .asResource())
        .isEqualTo(
            CapabilityStatement.CapabilityResource.builder()
                .type("type")
                .profile("url")
                .interaction(
                    List.of(
                        CapabilityStatement.ResourceInteraction.builder()
                            .code(CapabilityStatement.TypeRestfulInteraction.read)
                            .documentation(
                                "Implemented per specification. See http://hl7.org/fhir/R4/http.html")
                            .build(),
                        CapabilityStatement.ResourceInteraction.builder()
                            .code(CapabilityStatement.TypeRestfulInteraction.search_type)
                            .documentation(
                                "Implemented per specification. See http://hl7.org/fhir/R4/http.html")
                            .build()))
                .versioning(Versioning.no_version)
                .referencePolicy(List.of(ReferencePolicy.literal, ReferencePolicy.local))
                .searchParam(
                    List.of(
                        CapabilityStatement.SearchParam.builder()
                            .name("_id")
                            .type(SearchParamType.token)
                            .build()))
                .build());
  }
}
