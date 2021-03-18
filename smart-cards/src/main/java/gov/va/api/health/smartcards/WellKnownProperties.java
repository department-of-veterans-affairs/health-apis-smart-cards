package gov.va.api.health.smartcards;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("well-known")
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellKnownProperties {
  @Builder.Default
  private List<String> capabilities =
      List.of(
          "health-cards",
          "launch-standalone",
          "context-standalone-patient",
          "client-confidential-symmetric");

  @Builder.Default private List<String> responseTypeSupported = List.of("code, refresh_token");

  @Builder.Default
  private List<String> scopesSupported =
      List.of(
          "launch",
          "launch/patient",
          "patient/Patient.read",
          "patient/Immunization.read",
          "patient/Location.read",
          "offline_access");
}
