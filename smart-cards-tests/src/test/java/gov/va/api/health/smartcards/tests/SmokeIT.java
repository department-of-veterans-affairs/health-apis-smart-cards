package gov.va.api.health.smartcards.tests;

import static gov.va.api.health.smartcards.tests.SystemDefinitions.systemDefinition;

import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class SmokeIT {
  @Test
  void health() {
    SystemDefinitions.Service svc = systemDefinition().internal();
    String request = svc.urlWithApiPath() + "actuator/health";
    log.info(request);
    ExpectedResponse.of(
            RestAssured.given()
                .baseUri(svc.url())
                .port(svc.port())
                .relaxedHTTPSValidation()
                .request(Method.GET, request))
        .expect(200);
  }
}
