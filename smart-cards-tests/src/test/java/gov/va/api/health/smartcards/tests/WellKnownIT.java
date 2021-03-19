package gov.va.api.health.smartcards.tests;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;
import static gov.va.api.health.smartcards.tests.SystemDefinitions.systemDefinition;

import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class WellKnownIT {
  @BeforeAll
  static void assumeEnvironment() {
    assumeEnvironmentIn(
        Environment.LOCAL,
        Environment.QA,
        Environment.STAGING,
        Environment.STAGING_LAB,
        Environment.LAB);
  }

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
