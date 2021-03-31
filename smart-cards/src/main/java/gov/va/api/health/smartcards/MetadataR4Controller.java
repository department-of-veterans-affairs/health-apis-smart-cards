package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.datatypes.ContactDetail;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class MetadataR4Controller {
  private static final String NAME = "API Management Platform | Smart Cards - R4";

  private final BuildProperties buildProperties;

  private final LinkProperties pageLinks;

  private static List<ContactDetail> contact() {
    return List.of(
        ContactDetail.builder()
            .name("API Support")
            .telecom(
                List.of(
                    ContactPoint.builder()
                        .system(ContactPoint.ContactPointSystem.email)
                        .value("api@va.gov")
                        .build()))
            .build());
  }

  private static List<CapabilityStatement.Rest> rest() {
    return List.of(
        CapabilityStatement.Rest.builder()
            .mode(CapabilityStatement.RestMode.server)
            .resource(restResources())
            .build());
  }

  private static List<CapabilityStatement.CapabilityResource> restResources() {
    return List.of(
        CapabilityStatement.CapabilityResource.builder()
            .type("Parameters")
            .profile("https://www.hl7.org/fhir/r4/parameters.html")
            .interaction(
                List.of(
                    CapabilityStatement.ResourceInteraction.builder()
                        .code(CapabilityStatement.TypeRestfulInteraction.read)
                        .documentation(
                            "Implemented per specification. See http://hl7.org/fhir/R4/http.html")
                        .build()))
            .versioning(CapabilityStatement.Versioning.no_version)
            .referencePolicy(
                List.of(
                    CapabilityStatement.ReferencePolicy.literal,
                    CapabilityStatement.ReferencePolicy.local))
            .build());
  }

  private CapabilityStatement.Implementation implementation() {
    return CapabilityStatement.Implementation.builder()
        .description(NAME)
        .url(pageLinks.r4Url())
        .build();
  }

  @GetMapping(value = "/r4/metadata")
  CapabilityStatement read() {
    return CapabilityStatement.builder()
        .id("smart-cards-capability-statement")
        .version(buildProperties.getVersion())
        .name(NAME)
        .title(NAME)
        .status(CapabilityStatement.Status.active)
        .experimental(true)
        .date(buildProperties.getTime().toString())
        .publisher("Department of Veterans Affairs")
        .contact(contact())
        .description("Read and search support for smart health cards.")
        .kind(CapabilityStatement.Kind.capability)
        .software(software())
        .implementation(implementation())
        .fhirVersion("4.0.1")
        .format(List.of("application/json", "application/fhir+json"))
        .rest(rest())
        .build();
  }

  private CapabilityStatement.Software software() {
    return CapabilityStatement.Software.builder()
        .name(buildProperties.getGroup() + ":" + buildProperties.getArtifact())
        .version(buildProperties.getVersion())
        .releaseDate(buildProperties.getTime().toString())
        .build();
  }
}
