package gov.va.api.health.smartcards.tests;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;
import static gov.va.api.health.smartcards.tests.SystemDefinitions.systemDefinition;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Parameters;
import gov.va.api.health.r4.api.resources.Resource;
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
  private static final ObjectMapper MAPPER =
      JacksonConfig.createMapper().registerModule(new Resource.ResourceModule());

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
      SystemDefinitions.Service svc,
      String request,
      Object payload,
      String description,
      Integer expectedStatus) {
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
        svc.urlWithApiPath() + request,
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

  private static Parameters parametersCovid19() {
    return parametersWithCredentialType("https://smarthealth.cards#covid19");
  }

  private static Parameters parametersEmpty() {
    return Parameters.builder().build();
  }

  private static Parameters parametersWithCredentialType(String... credentialType) {
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
        systemDefinition().internal(),
        String.format("r4/Patient/%s/$HealthWallet.issueVc", id),
        parametersCovid19(),
        "issuevc",
        200);
  }

  @Test
  void read_externalDstu2() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    String id = systemDefinition().ids().patient();
    doPost(
        systemDefinition().external(),
        String.format("dstu2/Patient/%s/$HealthWallet.issueVc", id),
        parametersCovid19(),
        "issuevc",
        200);
  }

  @Test
  void read_externalR4() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    String id = systemDefinition().ids().patient();
    doPost(
        systemDefinition().external(),
        String.format("r4/Patient/%s/$HealthWallet.issueVc", id),
        parametersCovid19(),
        "issuevc",
        200);
  }

  @Test
  void read_invalid_badCredentialType() {
    String id = systemDefinition().ids().patient();
    String path = String.format("r4/Patient/%s/$HealthWallet.issueVc", id);
    var svc = systemDefinition().internal();
    doPost(
        svc,
        path,
        parametersWithCredentialType("NOPE"),
        "issueVc (invalid, bad credentialType)",
        400);
  }

  @Test
  void read_invalid_bodySchema() {
    String id = systemDefinition().ids().patient();
    String path = String.format("r4/Patient/%s/$HealthWallet.issueVc", id);
    var svc = systemDefinition().internal();
    doPost(svc, path, "{\"foo\":\"bar\"}", "issueVc (invalid, bad payload schema)", 400);
    doPost(svc, path, "NOPE", "issueVc (invalid, bad payload schema)", 400);
  }

  @Test
  void read_invalid_emptyBody() {
    String id = systemDefinition().ids().patient();
    String path = String.format("r4/Patient/%s/$HealthWallet.issueVc", id);
    var svc = systemDefinition().internal();
    doPost(svc, path, null, "issueVc(invalid, empty body)", 400);
  }

  @Test
  void read_invalid_parametersEmpty() {
    String id = systemDefinition().ids().patient();
    String path = String.format("r4/Patient/%s/$HealthWallet.issueVc", id);
    var svc = systemDefinition().internal();
    doPost(svc, path, parametersEmpty(), "issueVc (invalid, no parameters)", 400);
  }

  @Test
  void read_invalid_unimplementedCredentialType() {
    String id = systemDefinition().ids().patient();
    String path = String.format("r4/Patient/%s/$HealthWallet.issueVc", id);
    var svc = systemDefinition().internal();
    doPost(
        svc,
        path,
        parametersWithCredentialType(
            "https://smarthealth.cards#covid19", "https://smarthealth.cards#immunization"),
        "issueVc (invalid, unimplemented credentialType)",
        501);
  }

  @Test
  void read_notFound() {
    String id = systemDefinition().ids().patientNotFound();
    doPost(
        systemDefinition().internal(),
        String.format("r4/Patient/%s/$HealthWallet.issueVc", id),
        parametersCovid19(),
        "issuevc (not found)",
        404);
  }
}
