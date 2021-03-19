package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.information.WellKnown;
import java.util.List;
import org.junit.jupiter.api.Test;

public class WellKnownControllerTest {
  @Test
  void jwks() {
    var jwksProperties = JwsHelpers.jwksProperties("123");
    var controller = WellKnownController.builder().jwksProperties(jwksProperties).build();
    assertThat(controller.jwks()).isEqualTo(jwksProperties.jwksPublicJson());
  }

  @Test
  void smartConfiguration() {
    WellKnownController controller = WellKnownController.builder().build();
    assertThat(controller.smartConfiguration())
        .isEqualTo(
            WellKnown.builder()
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
                .build());
  }
}
