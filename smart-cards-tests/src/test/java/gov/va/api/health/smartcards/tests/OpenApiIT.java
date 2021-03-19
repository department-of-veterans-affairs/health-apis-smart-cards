package gov.va.api.health.smartcards.tests;

import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;
import static gov.va.api.health.smartcards.tests.SystemDefinitions.systemDefinition;

import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class OpenApiIT {
  @SneakyThrows
  private static ExpectedResponse doGet(
      SystemDefinitions.Service svc, String request, String description, Integer expectedStatus) {
    RequestSpecification spec =
        RestAssured.given()
            .baseUri(svc.url())
            .port(svc.port())
            .relaxedHTTPSValidation()
            .header("Content-Type", "application/json");
    log.info(
        "Expect {} GET '{}' is status code ({})",
        svc.urlWithApiPath() + request,
        description,
        expectedStatus);
    ExpectedResponse response =
        ExpectedResponse.of(spec.request(Method.GET, svc.urlWithApiPath() + request))
            .logAction(logAllWithTruncatedBody(2000));
    if (expectedStatus != null) {
      response.expect(expectedStatus);
    }
    return response;
  }

  @Test
  void dstu2OpenApi() {
    String path = "dstu2/openapi.json";
    var svc = systemDefinition().internal();
    doGet(svc, path, "R4 Open API Response", 200);
  }

  @Test
  void r4OpenApi() {
    String path = "r4/openapi.json";
    var svc = systemDefinition().internal();
    doGet(svc, path, "DSTU2 Open API Response", 200);
  }
}
