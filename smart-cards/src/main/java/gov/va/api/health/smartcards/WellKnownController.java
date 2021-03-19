package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.information.WellKnown;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = {"/.well-known/smart-configuration"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
@Builder
public class WellKnownController {

  @Builder.Default
  private List<String> capabilities =
      List.of(
          "health-cards",
          "launch-standalone",
          "context-standalone-patient",
          "client-confidential-symmetric");

  @Builder.Default private List<String> responseTypeSupported = List.of("code", "refresh_token");

  @Builder.Default
  private List<String> scopesSupported =
      List.of(
          "launch",
          "launch/patient",
          "patient/Patient.read",
          "patient/Immunization.read",
          "patient/Location.read",
          "offline_access");

  @GetMapping
  WellKnown read() {
    return WellKnown.builder()
        .capabilities(capabilities)
        .responseTypeSupported(responseTypeSupported)
        .scopesSupported(scopesSupported)
        .build();
  }
}
