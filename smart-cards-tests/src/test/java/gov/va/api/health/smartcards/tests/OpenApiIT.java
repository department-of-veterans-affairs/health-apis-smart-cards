package gov.va.api.health.smartcards.tests;

import static gov.va.api.health.smartcards.tests.Requests.doGet;
import static gov.va.api.health.smartcards.tests.SystemDefinitions.systemDefinition;

import org.junit.jupiter.api.Test;

public class OpenApiIT {
  @Test
  void dstu2OpenApi() {
    String path = "dstu2/openapi.json";
    var svc = systemDefinition().internal();
    doGet(svc, path, "open-api r4", 200);
  }

  @Test
  void r4OpenApi() {
    String path = "r4/openapi.json";
    var svc = systemDefinition().internal();
    doGet(svc, path, "open-api dstu2", 200);
  }
}
