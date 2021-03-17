package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.information.WellKnown;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = {"/.well-known/smart-configuration"},
    produces = {"application/json", "application/fhir+json", "application/json+fhir"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class WellKnownController {

  private final WellKnownProperties wellKnownProperties;
  private final MetadataProperties metadataProperties;

  @GetMapping
  WellKnown read() {
    return WellKnown.builder()
        .authorizationEndpoint(metadataProperties.getSecurity().getAuthorizeEndpoint())
        .tokenEndpoint(metadataProperties.getSecurity().getTokenEndpoint())
        .managementEndpoint(metadataProperties.getSecurity().getManagementEndpoint())
        .revocationEndpoint(metadataProperties.getSecurity().getRevocationEndpoint())
        .capabilities(wellKnownProperties.getCapabilities())
        .responseTypeSupported(wellKnownProperties.getResponseTypeSupported())
        .scopesSupported(wellKnownProperties.getScopesSupported())
        .build();
  }
}
