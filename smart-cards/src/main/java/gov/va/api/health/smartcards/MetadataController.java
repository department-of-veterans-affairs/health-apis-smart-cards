package gov.va.api.health.smartcards;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Conformance;
import gov.va.api.health.dstu2.api.resources.Conformance.Kind;
import gov.va.api.health.dstu2.api.resources.Conformance.ResourceInteractionCode;
import gov.va.api.health.dstu2.api.resources.Conformance.RestMode;
import gov.va.api.health.dstu2.api.resources.Conformance.RestResource;
import gov.va.api.health.dstu2.api.resources.Conformance.RestResourceVersion;
import gov.va.api.health.dstu2.api.resources.Conformance.Status;
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
@RequestMapping
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

  private static List<Conformance.Contact> contactDstu2() {
    return List.of(
        Conformance.Contact.builder()
            .name("API Support")
            .telecom(
                List.of(
                    gov.va.api.health.dstu2.api.datatypes.ContactPoint.builder()
                        .system(
                            gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointSystem
                                .email)
                        .value("api@va.gov")
                        .build()))
            .build());
  }

  private static List<CapabilityStatement.CapabilityResource> resources() {
    return Stream.of(
            SupportedResource.builder()
                .type("Parameters")
                .profileUrl("https://www.hl7.org/fhir/r4/parameters.html")
                .build())
        .map(SupportedResource::asResource)
        .collect(toList());
  }

  private static List<Conformance.RestResource> resourcesDstu2() {
    return Stream.of(
            SupportedResource.builder()
                .type("Parameters")
                .profileUrl("https://www.hl7.org/fhir/r4/parameters.html")
                .build())
        .map(SupportedResource::asConformanceResource)
        .collect(toList());
  }

  private static List<CapabilityStatement.Rest> rest() {
    return List.of(
        CapabilityStatement.Rest.builder()
            .mode(CapabilityStatement.RestMode.server)
            .resource(resources())
            .build());
  }

  private static List<Conformance.Rest> restDstu2() {
    return List.of(
        Conformance.Rest.builder().mode(RestMode.server).resource(resourcesDstu2()).build());
  }

  private CapabilityStatement.Implementation implementation() {
    return CapabilityStatement.Implementation.builder()
        .description(NAME)
        .url(pageLinks.r4Url())
        .build();
  }

  private Conformance.Implementation implementationDstu2() {
    return Conformance.Implementation.builder().description(NAME).url(pageLinks.r4Url()).build();
  }

  @GetMapping(
      path = "r4/metadata",
      produces = {"application/json", "application/fhir+json"})
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

  @GetMapping(
      path = "dstu2/metadata",
      produces = {"application/json", "application/fhir+json"})
  Conformance readDstu2() {
    return Conformance.builder()
        .resourceType("Conformance")
        .id("smart-cards-conformance")
        .version("1.0.2")
        .name(NAME)
        .publisher("Department of Veterans Affairs")
        .status(Status.active)
        .implementation(implementationDstu2())
        .experimental(true)
        .contact(contactDstu2())
        .date(buildProperties.getTime().toString())
        .description("Read and search support for credentials of immunization by patient.")
        .kind(Kind.capability)
        .software(softwareDstu2())
        .fhirVersion("4.0.1")
        .format(List.of("application/json", "application/fhir+json"))
        .rest(restDstu2())
        .build();
  }

  private CapabilityStatement.Software software() {
    return CapabilityStatement.Software.builder()
        .name(buildProperties.getGroup() + ":" + buildProperties.getArtifact())
        .releaseDate(buildProperties.getTime().toString())
        .version(buildProperties.getVersion())
        .build();
  }

  private Conformance.Software softwareDstu2() {
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

    Conformance.RestResource asConformanceResource() {
      return RestResource.builder()
          .type(type)
          .profile(Reference.builder().reference(profileUrl).build())
          .interaction(interactionsDstu2())
          .versioning(RestResourceVersion.no_version)
          .build();
    }

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

    private List<Conformance.ResourceInteraction> interactionsDstu2() {
      Conformance.ResourceInteraction readable =
          Conformance.ResourceInteraction.builder()
              .code(ResourceInteractionCode.read)
              .documentation("Implemented per specification. See http://hl7.org/fhir/R4/http.html")
              .build();
      return List.of(readable);
    }
  }
}
