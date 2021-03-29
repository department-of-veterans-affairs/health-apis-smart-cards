package gov.va.api.health.smartcards.tests;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static gov.va.api.health.smartcards.tests.Requests.doGet;
import static gov.va.api.health.smartcards.tests.SystemDefinitions.systemDefinition;

import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.Test;

public class WellKnownIT {
  @Test
  void jwks_external() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    doGet(systemDefinition().external(), "dstu2/.well-known/jwks.json", "jwks dstu2", 200);
    doGet(systemDefinition().external(), "r4/.well-known/jwks.json", "jwks r4", 200);
  }

  @Test
  void jwks_internal() {
    doGet(systemDefinition().internal(), ".well-known/jwks.json", "jwks", 200);
  }

  @Test
  void jwks_notFound() {
    doGet(systemDefinition().internal(), ".well-known/jwks-no-route.json", "jwks not found", 404);
  }

  @Test
  void smartConfiguration() {
    doGet(
        systemDefinition().internal(),
        ".well-known/smart-configuration",
        "smart configuration",
        200);
  }
}
