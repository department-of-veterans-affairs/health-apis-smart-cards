package gov.va.api.health.smartcards.tests;

import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;

import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Requests {
  static ExpectedResponse doGet(
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
}
