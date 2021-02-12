package gov.va.api.health.smartcards.tests;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;
import static gov.va.api.health.smartcards.tests.SystemDefinitions.systemDefinition;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class PatientIT {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private static final String ACCESS_TOKEN = System.getProperty("access-token", "unset");

  @BeforeAll
  static void assumeEnvironment() {
    assumeEnvironmentIn(
        Environment.LOCAL,
        Environment.QA,
        Environment.STAGING,
        Environment.STAGING_LAB,
        Environment.LAB);
  }

  @SneakyThrows
  private static ExpectedResponse doPost(
      String request, Object payload, String description, Integer expectedStatus) {
    SystemDefinitions.Service svc = systemDefinition().internal();
    RequestSpecification spec =
        RestAssured.given()
            .baseUri(svc.url())
            .port(svc.port())
            .relaxedHTTPSValidation()
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .header("Content-Type", "application/json")
            .body(MAPPER.writeValueAsString(payload));
    log.info(
        "Expect {} POST '{}' is status code ({})",
        svc.apiPath() + request,
        description,
        expectedStatus);
    ExpectedResponse response =
        ExpectedResponse.of(spec.request(Method.POST, svc.urlWithApiPath() + request))
            .logAction(logAllWithTruncatedBody(2000));
    if (expectedStatus != null) {
      response.expect(expectedStatus);
    }
    return response;
  }

  @Test
  void read() {
    String id = systemDefinition().ids().patient();
    doPost(String.format("r4/Patient/%s/$HealthWallet.issueVc", id), "{}", "issuevc", 200);
  }

  @Test
  void read_notFound() {
    String id = systemDefinition().ids().patientNotFound();
    doPost(
        String.format("r4/Patient/%s/$HealthWallet.issueVc", id), "{}", "issuevc (not found)", 404);
  }
}
