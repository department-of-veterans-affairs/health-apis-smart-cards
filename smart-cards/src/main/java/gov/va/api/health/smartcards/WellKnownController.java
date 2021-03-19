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

  @GetMapping
  WellKnown read() {
    return WellKnown.builder()
        .capabilities(
            List.of(
                "health-cards",
                "launch-standalone",
                "context-standalone-patient",
                "client-confidential-symmetric"))
        .responseTypeSupported(List.of("code", "refresh_token"))
        .scopesSupported(
            List.of(
                "launch",
                "launch/patient",
                "patient/Patient.read",
                "patient/Immunization.read",
                "patient/Location.read",
                "offline_access"))
        .build();
  }
}
