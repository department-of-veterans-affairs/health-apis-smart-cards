package gov.va.api.health.smartcards;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.information.WellKnown;
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
    WellKnownController controller = new WellKnownController(wellKnownProperties());
    assertThat(pretty(controller.read())).isEqualTo(pretty(actual()));
  }

  private WellKnownProperties wellKnownProperties() {
    return WellKnownProperties.builder()
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
}
