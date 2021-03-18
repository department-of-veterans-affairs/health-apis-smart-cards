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
import java.util.List;
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
    return doPost(svc, spec, request, payload, description, expectedStatus);
  }

  private static ExpectedResponse doPost(
      SystemDefinitions.Service svc,
      RequestSpecification spec,
      String request,
      Object payload,
      String description,
      Integer expectedStatus) {
    log.info(
        "Expect {} POST '{}' is status code ({})",
        svc.urlWithApiPath() + request,
        description,
        expectedStatus);
    ExpectedResponse response =
        ExpectedResponse.of(spec.request(Method.POST, svc.urlWithApiPath() + request))
            .logAction(logAllWithTruncatedBody(2000))
            .mapper(MAPPER);
    if (expectedStatus != null) {
      response.expect(expectedStatus);
    }
    return response;
  }

  private static Parameters parametersCovid19() {
    return parametersWithCredentialType("https://smarthealth.cards#covid19");
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
        String.format("r4/Patient/%s/$health-cards-issue", id),
        parametersCovid19(),
        "$health-cards-issue",
        200);
  }

  @Test
  void read_external_dstu2() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    String id = systemDefinition().ids().patient();
    doPost(
        systemDefinition().external(),
        String.format("dstu2/Patient/%s/$health-cards-issue", id),
        parametersCovid19(),
        "$health-cards-issue",
        200);
  }

  @Test
  void read_external_r4() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    String id = systemDefinition().ids().patient();
    doPost(
        systemDefinition().external(),
        String.format("r4/Patient/%s/$health-cards-issue", id),
        parametersCovid19(),
        "$health-cards-issue",
        200);
  }

  @Test
  void read_invalid_beanValidation() {
    String id = systemDefinition().ids().patient();
    String path = String.format("r4/Patient/%s/$health-cards-issue", id);
    var svc = systemDefinition().internal();
    var parameters =
        Parameters.builder().parameter(List.of(Parameters.Parameter.builder().build())).build();
    doPost(svc, path, parameters, "$health-cards-issue (parameter bean validation)", 400);
  }

  @Test
  void read_invalid_bodySchema() {
    String id = systemDefinition().ids().patient();
    String path = String.format("r4/Patient/%s/$health-cards-issue", id);
    var svc = systemDefinition().internal();
    String desc = "$health-cards-issue (bad payload schema)";
    doPost(svc, path, "{\"foo\":\"bar\"}", desc, 400);
    doPost(svc, path, "NOPE", desc, 400);
  }

  @Test
  void read_invalid_emptyBody() {
    String id = systemDefinition().ids().patient();
    String path = String.format("r4/Patient/%s/$health-cards-issue", id);
    var svc = systemDefinition().internal();
    doPost(svc, path, null, "$health-cards-issue (empty body)", 400);
  }

  @Test
  @SneakyThrows
  void read_invalid_noToken() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    String id = systemDefinition().ids().patient();
    var svc = systemDefinition().internal();
    Parameters payload = parametersCovid19();
    RequestSpecification spec =
        RestAssured.given()
            .baseUri(svc.url())
            .port(svc.port())
            .relaxedHTTPSValidation()
            .header("Content-Type", "application/json")
            .body(MAPPER.writeValueAsString(payload));
    doPost(
        systemDefinition().internal(),
        spec,
        String.format("r4/Patient/%s/$health-cards-issue", id),
        payload,
        "$health-cards-issue (no token)",
        401);
  }

  @Test
  void read_invalid_parametersEmpty() {
    String id = systemDefinition().ids().patient();
    String path = String.format("r4/Patient/%s/$health-cards-issue", id);
    var svc = systemDefinition().internal();
    var empty = Parameters.builder().build();
    doPost(svc, path, empty, "$health-cards-issue (no parameters)", 400);
  }

  @Test
  void read_invalid_patientNotMe() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    doPost(
        systemDefinition().internal(),
        String.format("r4/Patient/%s/$health-cards-issue", "5555555555555"),
        parametersCovid19(),
        "$health-cards-issue (patient not-me)",
        403);
  }

  @Test
  void read_invalid_unimplementedCredentialType() {
    String id = systemDefinition().ids().patient();
    String path = String.format("r4/Patient/%s/$health-cards-issue", id);
    var svc = systemDefinition().internal();
    doPost(
        svc,
        path,
        parametersWithCredentialType(
            "https://smarthealth.cards#covid19", "https://smarthealth.cards#immunization"),
        "$health-cards-issue (unimplemented credentialType)",
        501);
  }

  @Test
  void read_invalid_unknownCredentialType() {
    String id = systemDefinition().ids().patient();
    String path = String.format("r4/Patient/%s/$health-cards-issue", id);
    var svc = systemDefinition().internal();
    doPost(
        svc,
        path,
        parametersWithCredentialType("NOPE"),
        "$health-cards-issue (unknown credentialType)",
        400);
  }
}
