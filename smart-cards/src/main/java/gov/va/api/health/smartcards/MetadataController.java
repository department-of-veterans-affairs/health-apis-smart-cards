package gov.va.api.health.smartcards;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.datatypes.ContactDetail;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = {"/r4/metadata", "/dstu2/metadata"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class MetadataController {
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

  private static List<CapabilityStatement.CapabilityResource> resources() {
    return Stream.of(
            SupportedResource.builder()
                .type("Patient")
                .profileUrl("https://www.hl7.org/fhir/r4/patient.html")
                .build())
        .map(SupportedResource::asResource)
        .collect(toList());
  }

  private static List<CapabilityStatement.Rest> rest() {
    return List.of(
        CapabilityStatement.Rest.builder()
            .mode(CapabilityStatement.RestMode.server)
            .resource(resources())
            .build());
  }

  private CapabilityStatement.Implementation implementation() {
    return CapabilityStatement.Implementation.builder()
        .description(NAME)
        .url(pageLinks.r4Url())
        .build();
  }

  @GetMapping
  CapabilityStatement read() {
    return CapabilityStatement.builder()
        .resourceType("CapabilityStatement")
        .id("smart-cards-capability-statement")
        .version("1.0.2")
        .name(NAME)
        .title(NAME)
        .publisher("Department of Veterans Affairs")
        .status(CapabilityStatement.Status.active)
        .implementation(implementation())
        .experimental(true)
        .contact(contact())
        .date(buildProperties.getTime().toString())
        .description("Read and search support for credentials of immunization by patient.")
        .kind(CapabilityStatement.Kind.capability)
        .software(software())
        .fhirVersion("4.0.1")
        .format(List.of("application/json", "application/fhir+json"))
        .rest(rest())
        .build();
  }

  private CapabilityStatement.Software software() {
    return CapabilityStatement.Software.builder()
        .name(buildProperties.getGroup() + ":" + buildProperties.getArtifact())
        .releaseDate(buildProperties.getTime().toString())
        .version(buildProperties.getVersion())
        .build();
  }

  @Value
  @Builder
  static final class SupportedResource {
    String type;

    String profileUrl;

    CapabilityStatement.CapabilityResource asResource() {
      return CapabilityStatement.CapabilityResource.builder()
          .type(type)
          .profile(profileUrl)
          .interaction(interactions())
          .versioning(CapabilityStatement.Versioning.no_version)
          .referencePolicy(
              List.of(
                  CapabilityStatement.ReferencePolicy.literal,
                  CapabilityStatement.ReferencePolicy.local))
          .build();
    }

    private List<CapabilityStatement.ResourceInteraction> interactions() {
      CapabilityStatement.ResourceInteraction readable =
          CapabilityStatement.ResourceInteraction.builder()
              .code(CapabilityStatement.TypeRestfulInteraction.read)
              .documentation("Implemented per specification. See http://hl7.org/fhir/R4/http.html")
              .build();
      return List.of(readable);
    }
  }
}
