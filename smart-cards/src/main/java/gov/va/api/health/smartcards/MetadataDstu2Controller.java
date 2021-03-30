package gov.va.api.health.smartcards;

import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Conformance;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = "/dstu2/metadata",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class MetadataDstu2Controller {
  private static final String NAME = "API Management Platform | Smart Cards - R4";

  private final BuildProperties buildProperties;

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

  private static List<Conformance.Rest> rest() {
    return List.of(
        Conformance.Rest.builder()
            .mode(Conformance.RestMode.server)
            .resource(restResources())
            .build());
  }

  private static List<Conformance.RestResource> restResources() {
    return List.of(
        Conformance.RestResource.builder()
            .type("Parameters")
            .profile(
                Reference.builder()
                    .reference("https://www.hl7.org/fhir/r4/parameters.html")
                    .build())
            .interaction(
                List.of(
                    Conformance.ResourceInteraction.builder()
                        .code(Conformance.ResourceInteractionCode.read)
                        .documentation(
                            "Implemented per specification. See http://hl7.org/fhir/R4/http.html")
                        .build()))
            .versioning(Conformance.RestResourceVersion.no_version)
            .build());
  }

  @GetMapping
  Conformance read() {
    return Conformance.builder()
        .id("smart-cards-conformance")
        .version(buildProperties.getVersion())
        .name(NAME)
        .publisher("Department of Veterans Affairs")
        .contact(contact())
        .date(buildProperties.getTime().toString())
        .description("Read and search support for smart health cards.")
        .kind(Conformance.Kind.capability)
        .software(software())
        .fhirVersion("4.0.1")
        .acceptUnknown(Conformance.AcceptUnknown.no)
        .format(List.of("application/json", "application/fhir+json"))
        .rest(rest())
        .build();
  }

  private Conformance.Software software() {
    return Conformance.Software.builder()
        .name(buildProperties.getGroup() + ":" + buildProperties.getArtifact())
        .build();
  }
}
