package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.information.WellKnown;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class WellKnownControllerTest {
  private WellKnown actual() {
    List<String> capabilities =
        List.of(
            "health-cards",
            "launch-standalone",
            "context-standalone-patient",
            "client-confidential-symmetric");
    List<String> responseTypeSupported = (List.of("code", "refresh_token"));
    List<String> scopesSupported =
        List.of(
            "launch",
            "launch/patient",
            "patient/Patient.read",
            "patient/Immunization.read",
            "patient/Location.read",
            "offline_access");
    return WellKnown.builder()
        .capabilities(capabilities)
        .responseTypeSupported(responseTypeSupported)
        .scopesSupported(scopesSupported)
        .build();
  }

  @SneakyThrows
  private String pretty(WellKnown wellKnown) {
    return JacksonConfig.createMapper()
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(wellKnown);
  }

  @Test
  @SneakyThrows
  public void read() {
    WellKnownController controller = WellKnownController.builder().build();
    assertThat(pretty(controller.read())).isEqualTo(pretty(actual()));
  }
}
