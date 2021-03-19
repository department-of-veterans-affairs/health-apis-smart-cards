package gov.va.api.health.smartcards.tests;

import static gov.va.api.health.smartcards.tests.Requests.doGet;
import static gov.va.api.health.smartcards.tests.SystemDefinitions.systemDefinition;

import org.junit.jupiter.api.Test;

public class MetadataIT {
  @Test
  void dstu2Metadata() {
    String path = "dstu2/metadata";
    var svc = systemDefinition().internal();
    doGet(svc, path, "metadata dstu2", 200);
  }

  @Test
  void r4Metadata() {
    String path = "r4/metadata";
    var svc = systemDefinition().internal();
    doGet(svc, path, "metadata r4", 200);
  }
}
