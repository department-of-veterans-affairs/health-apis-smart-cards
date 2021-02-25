package gov.va.api.health.smartcards.tests;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;
import static gov.va.api.health.smartcards.tests.SystemDefinitions.systemDefinition;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Parameters;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import java.util.Arrays;
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
      String request,
      Object payload,
      String description,
      Integer expectedStatus,
      SystemDefinitions.Service svc) {
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

  private Parameters parametersCovid19() {
    return parametersWithCredentialType("https://smarthealth.cards#covid19");
  }

  private Parameters parametersEmpty() {
    return Parameters.builder().build();
  }

  private Parameters parametersWithCredentialType(String... credentialType) {
    return Parameters.builder()
        .parameter(
            Arrays.stream(credentialType)
                .map(c -> Parameters.Parameter.builder().name("credentialType").valueUri(c).build())
                .collect(toList()))
        .build();
  }

  @Test
  void read() {
    String id = systemDefinition().ids().patient();
    doPost(
        String.format(systemDefinition().ids().r4Path(), id),
        parametersCovid19(),
        "issuevc",
        200,
        systemDefinition().internal());
  }

  @Test
  void readDstu2() {
    String id = systemDefinition().ids().patient();
    doPost(
        String.format(systemDefinition().ids().dstu2Path(), id),
        parametersCovid19(),
        "issuevc",
        200,
        systemDefinition().external());
  }

  @Test
  void read_invalidParameters() {
    String id = systemDefinition().ids().patient();
    String path = String.format(systemDefinition().ids().r4Path(), id);
    // Empty body
    doPost(path, null, "issueVc(invalid, empty body)", 400, systemDefinition().internal());
    // Bad body schema
    doPost(
        path,
        "{\"foo\": \"bar\"}",
        "issueVc (invalid, bad payload schema)",
        400,
        systemDefinition().internal());
    doPost(
        path, "NOPE", "issueVc (invalid, bad payload schema)", 400, systemDefinition().internal());
    // Parameters object without any parameters
    doPost(
        path,
        parametersEmpty(),
        "issueVc (invalid, no parameters)",
        400,
        systemDefinition().internal());
    // Parameters object with invalid credentialType
    doPost(
        path,
        parametersWithCredentialType("NOPE"),
        "issueVc (invalid, bad credentialType)",
        400,
        systemDefinition().internal());

    // Parameters object with unimplemented credentialType (immunization)
    doPost(
        path,
        parametersWithCredentialType(
            "https://smarthealth.cards#covid19", "https://smarthealth.cards#immunization"),
        "issueVc (invalid, unimplemented credentialType)",
        501,
        systemDefinition().internal());
  }

  @Test
  void read_notFound() {
    String id = systemDefinition().ids().patientNotFound();
    doPost(
        String.format(systemDefinition().ids().r4Path(), id),
        parametersCovid19(),
        "issuevc (not found)",
        404,
        systemDefinition().internal());
  }
}
