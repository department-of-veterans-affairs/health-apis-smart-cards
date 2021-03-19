package gov.va.api.health.smartcards;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.information.WellKnown;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class WellKnownControllerTest {
  private WellKnown actual() {
    return WellKnown.builder()
        .capabilities(
            asList(
                "context-standalone-patient",
                "launch-standalone",
                "permission-offline",
                "permission-patient"))
        .responseTypeSupported(asList("code", "refresh-token"))
        .scopesSupported(asList("patient/$HealthWallet.issueVc"))
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
    List<String> capabilities =
        List.of(
            "context-standalone-patient",
            "launch-standalone",
            "permission-offline",
            "permission-patient");
    List<String> responseTypeSupported = List.of("code", "refresh-token");
    List<String> scopesSupported = List.of("patient/$HealthWallet.issueVc");
    WellKnownController controller =
        WellKnownController.builder()
            .capabilities(capabilities)
            .responseTypeSupported(responseTypeSupported)
            .scopesSupported(scopesSupported)
            .build();
    assertThat(pretty(controller.read())).isEqualTo(pretty(actual()));
  }
}
