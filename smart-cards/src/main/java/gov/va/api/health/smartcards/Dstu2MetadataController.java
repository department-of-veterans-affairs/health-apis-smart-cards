package gov.va.api.health.smartcards;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Conformance;
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
    value = {"/dstu2/metadata"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class Dstu2MetadataController {

  private static final String NAME = "API Management Platform | Smart Cards - R4";

  private final BuildProperties buildProperties;

  private final LinkProperties pageLinks;

  private static List<Conformance.Contact> contact() {
    return List.of(
        Conformance.Contact.builder()
            .name("API Support")
            .telecom(
                List.of(
                    ContactPoint.builder()
                        .system(ContactPoint.ContactPointSystem.email)
                        .value("api@va.gov")
                        .build()))
            .build());
  }

  private static List<Conformance.RestResource> resources() {
    return Stream.of(
            SupportedResource.builder()
                .type("Parameters")
                .profileUrl("https://www.hl7.org/fhir/r4/parameters.html")
                .build())
        .map(SupportedResource::asResource)
        .collect(toList());
  }

  private static List<Conformance.Rest> rest() {
    return List.of(
        Conformance.Rest.builder().mode(Conformance.RestMode.server).resource(resources()).build());
  }

  private Conformance.Implementation implementation() {
    return Conformance.Implementation.builder().description(NAME).url(pageLinks.r4Url()).build();
  }

  @GetMapping
  Conformance read() {
    return Conformance.builder()
        .resourceType("Conformance")
        .id("smart-cards-conformance")
        .version("1.0.2")
        .name(NAME)
        .publisher("Department of Veterans Affairs")
        .status(Conformance.Status.active)
        .implementation(implementation())
        .experimental(true)
        .contact(contact())
        .date(buildProperties.getTime().toString())
        .description("Read and search support for credentials of immunization by patient.")
        .kind(Conformance.Kind.capability)
        .software(software())
        .fhirVersion("4.0.1")
        .format(List.of("application/json", "application/fhir+json"))
        .rest(rest())
        .build();
  }

  private Conformance.Software software() {
    return Conformance.Software.builder()
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

    Conformance.RestResource asResource() {
      return Conformance.RestResource.builder()
          .type(type)
          .profile(Reference.builder().reference(profileUrl).build())
          .interaction(interactions())
          .versioning(Conformance.RestResourceVersion.no_version)
          .build();
    }

    private List<Conformance.ResourceInteraction> interactions() {
      Conformance.ResourceInteraction readable =
          Conformance.ResourceInteraction.builder()
              .code(Conformance.ResourceInteractionCode.read)
              .documentation("Implemented per specification. See http://hl7.org/fhir/R4/http.html")
              .build();
      return List.of(readable);
    }
  }
}
