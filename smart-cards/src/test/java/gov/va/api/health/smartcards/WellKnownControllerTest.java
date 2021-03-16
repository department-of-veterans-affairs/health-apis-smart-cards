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
        .tokenEndpoint("https://somevasite.va.gov/token")
        .authorizationEndpoint("https://somevasite.va.gov/authorize")
        .managementEndpoint("https://somevasite.va.gov/manage")
        .revocationEndpoint("https://somevasite.va.gov/revoke")
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

  private MetadataProperties conformanceProperties() {
    return MetadataProperties.builder()
        .security(
            MetadataProperties.SecurityProperties.builder()
                .authorizeEndpoint("https://somevasite.va.gov/authorize")
                .tokenEndpoint("https://somevasite.va.gov/token")
                .managementEndpoint("https://somevasite.va.gov/manage")
                .revocationEndpoint("https://somevasite.va.gov/revoke")
                .build())
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
    WellKnownController controller =
        new WellKnownController(wellKnownProperties(), conformanceProperties());
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
