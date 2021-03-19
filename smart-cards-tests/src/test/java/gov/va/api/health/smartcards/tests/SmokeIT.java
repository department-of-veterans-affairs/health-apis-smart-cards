package gov.va.api.health.smartcards.tests;

import static gov.va.api.health.smartcards.tests.Requests.doGet;
import static gov.va.api.health.smartcards.tests.SystemDefinitions.systemDefinition;

import org.junit.jupiter.api.Test;

public class SmokeIT {
  @Test
  void health() {
    doGet(systemDefinition().internal(), "actuator/health", "actuator", 200);
  }
}
