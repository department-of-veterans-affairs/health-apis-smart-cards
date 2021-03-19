package gov.va.api.health.smartcards.tests;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;
import static gov.va.api.health.smartcards.tests.SystemDefinitions.systemDefinition;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class WellKnownJwksIT {
  private static final ObjectMapper MAPPER =
      JacksonConfig.createMapper().registerModule(new Resource.ResourceModule());

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
      SystemDefinitions.Service svc, String request, Integer expectedStatus) {
    RequestSpecification spec =
        RestAssured.given().baseUri(svc.url()).port(svc.port()).relaxedHTTPSValidation();
    log.info("Expect {} GET is status code ({})", svc.urlWithApiPath() + request, expectedStatus);
    ExpectedResponse response =
        ExpectedResponse.of(spec.request(Method.GET, svc.urlWithApiPath() + request))
            .logAction(logAllWithTruncatedBody(2000))
            .mapper(MAPPER);
    if (expectedStatus != null) {
      response.expect(expectedStatus);
    }
    return response;
  }

  @Test
  public void jwks_external() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    doGet(systemDefinition().external(), "r4/.well-known/jwks.json", 200);
    doGet(systemDefinition().external(), "dstu2/.well-known/jwks.json", 200);
  }

  @Test
  public void jwks_internal() {
    var internal = systemDefinition().internal();
    doGet(internal, ".well-known/jwks.json", 200);
    doGet(internal, ".well-known/jwks-no-route.json", 404);
  }
}
