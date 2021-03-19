package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.information.WellKnown;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Builder
@RestController
@RequestMapping(produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class WellKnownController {
  private final JwksProperties jwksProperties;

  @GetMapping(value = "/.well-known/jwks.json")
  public String jwks() {
    return jwksProperties.jwksPublicJson();
  }

  @GetMapping(value = "/.well-known/smart-configuration")
  WellKnown smartConfiguration() {
    return WellKnown.builder()
        .capabilities(
            List.of(
                "client-confidential-symmetric",
                "context-standalone-patient",
                "health-cards",
                "launch-standalone"))
        .responseTypeSupported(List.of("code", "refresh_token"))
        .scopesSupported(
            List.of(
                "launch",
                "launch/patient",
                "offline_access",
                "patient/Immunization.read",
                "patient/Location.read",
                "patient/Patient.read"))
        .build();
  }
}
