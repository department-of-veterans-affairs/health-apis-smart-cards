package gov.va.api.health.smartcards.tests;

import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.SentinelProperties;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
class SystemDefinitions {
  private static SystemDefinition lab() {
    return SystemDefinition.builder()
        .internal(
            serviceDefinition(
                "internal", "https://blue.lab.lighthouse.va.gov", 443, "/smart-cards/"))
        .build();
  }

  private static SystemDefinition local() {
    return SystemDefinition.builder()
        .internal(serviceDefinition("internal", "http://localhost", 8096, "/"))
        .build();
  }

  private static SystemDefinition production() {
    return SystemDefinition.builder()
        .internal(
            serviceDefinition(
                "internal", "https://blue.production.lighthouse.va.gov", 443, "/smart-cards/"))
        .build();
  }

  private static SystemDefinition qa() {
    return SystemDefinition.builder()
        .internal(
            serviceDefinition(
                "internal", "https://blue.qa.lighthouse.va.gov", 443, "/smart-cards/"))
        .build();
  }

  private static Service serviceDefinition(String name, String url, int port, String apiPath) {
    return Service.builder()
        .url(SentinelProperties.optionUrl(name, url))
        .port(port)
        .apiPath(SentinelProperties.optionApiPath(name, apiPath))
        .build();
  }

  private static SystemDefinition staging() {
    return SystemDefinition.builder()
        .internal(
            serviceDefinition(
                "internal", "https://blue.staging.lighthouse.va.gov", 443, "/smart-cards/"))
        .build();
  }

  private static SystemDefinition stagingLab() {
    return SystemDefinition.builder()
        .internal(
            serviceDefinition(
                "internal", "https://blue.staging-lab.lighthouse.va.gov", 443, "/smart-cards/"))
        .build();
  }

  static SystemDefinition systemDefinition() {
    switch (Environment.get()) {
      case LOCAL:
        return local();
      case QA:
        return qa();
      case STAGING:
        return staging();
      case PROD:
        return production();
      case STAGING_LAB:
        return stagingLab();
      case LAB:
        return lab();
      default:
        throw new IllegalArgumentException(
            "Unsupported sentinel environment: " + Environment.get());
    }
  }

  @Value
  @Builder
  static final class Service {
    @NonNull String url;

    @NonNull Integer port;

    @NonNull String apiPath;

    String urlWithApiPath() {
      StringBuilder builder = new StringBuilder(url());
      if (!apiPath().startsWith("/")) {
        builder.append('/');
      }
      builder.append(apiPath());
      if (!apiPath.endsWith("/")) {
        builder.append('/');
      }
      return builder.toString();
    }
  }

  @Value
  @Builder
  static final class SystemDefinition {
    @NonNull Service internal;
  }
}