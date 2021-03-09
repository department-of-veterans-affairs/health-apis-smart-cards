package gov.va.api.health.smartcards.patient;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Parameters;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.smartcards.DataQueryFhirClient;
import gov.va.api.health.smartcards.Exceptions;
import gov.va.api.health.smartcards.JacksonMapperConfig;
import gov.va.api.health.smartcards.LinkProperties;
import gov.va.api.health.smartcards.MockFhirClient;
import gov.va.api.health.smartcards.R4MixedBundler;
import gov.va.api.health.smartcards.vc.VerifiableCredential;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.web.client.RestTemplate;

public class PatientControllerTest {
  public static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  private static LinkProperties _linkProperties() {
    return LinkProperties.builder()
        .dqInternalUrl("http://dq.foo")
        .dqInternalR4BasePath("r4")
        .baseUrl("http://sc.bar")
        .r4BasePath("r4")
        .build();
  }

  private static long countEntriesByType(MixedBundle bundle, String type) {
    checkNotNull(bundle);
    return bundle.entry().stream()
        .filter(e -> e.resource().getClass().getSimpleName().equals(type))
        .count();
  }

  private static List<Parameters.Parameter> findResourceLinksFromParameters(Parameters parameters) {
    return parameters.parameter().stream()
        .filter(p -> "resourceLink".equals(p.name()))
        .collect(toList());
  }

  @SneakyThrows
  private static VerifiableCredential findVcFromParameters(Parameters parameters) {
    var maybeParam =
        parameters.parameter().stream()
            .filter(p -> "verifiableCredential".equals(p.name()))
            .findFirst();
    var parameter = maybeParam.orElseThrow();
    return MAPPER.readValue(parameter.valueString(), VerifiableCredential.class);
  }

  @BeforeAll
  static void init() {
    Patient.IDENTIFIER_MIN_SIZE.set(0);
  }

  static Patient.Bundle mockPatient(String icn) {
    var mockFhirClient = new MockFhirClient(_linkProperties());
    return mockFhirClient.patientBundle(icn, "");
  }

  private static Parameters parametersCovid19() {
    return parametersWithCredentialType("https://smarthealth.cards#covid19");
  }

  private static Parameters parametersEmpty() {
    return Parameters.builder().parameter(List.of()).build();
  }

  private static Parameters parametersWithCredentialType(String... credentialType) {
    return Parameters.builder()
        .parameter(
            Arrays.stream(credentialType)
                .map(c -> Parameters.Parameter.builder().name("credentialType").valueUri(c).build())
                .collect(toList()))
        .build();
  }

  private static PatientController patientController(
      ResponseEntity<Patient.Bundle> patientBundleResponse) {
    var mockRestTemplate = mock(RestTemplate.class);
    if (patientBundleResponse != null) {
      when(mockRestTemplate.exchange(
              anyString(),
              any(HttpMethod.class),
              any(),
              ArgumentMatchers.<Class<Patient.Bundle>>any()))
          .thenReturn(patientBundleResponse);
    }
    var mockFhirClient = new MockFhirClient(_linkProperties());
    var fhirClient = new DataQueryFhirClient(mockRestTemplate, _linkProperties());
    var bundler = new R4MixedBundler();
    return new PatientController(fhirClient, mockFhirClient, bundler);
  }

  @Test
  void initDirectFieldAccess() {
    new PatientController(
            mock(DataQueryFhirClient.class), mock(MockFhirClient.class), mock(R4MixedBundler.class))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @Test
  void issueVc() {
    var patientBundleResponse = new ResponseEntity<>(mockPatient("123"), HttpStatus.ACCEPTED);
    var controller = patientController(patientBundleResponse);
    var result = controller.issueVc("123", parametersCovid19(), "").getBody();
    assertNotNull(result);
    var vc = findVcFromParameters(result);
    assertThat(vc.context()).isEqualTo(List.of("https://www.w3.org/2018/credentials/v1"));
    assertThat(vc.type()).contains("VerifiableCredential", "https://smarthealth.cards#covid19");
    var fhirBundle = vc.credentialSubject().fhirBundle();
    assertThat(fhirBundle.entry()).hasSize(fhirBundle.total());
    assertThat(fhirBundle.total()).isEqualTo(4);
    assertThat(countEntriesByType(fhirBundle, "Patient")).isEqualTo(1);
    assertThat(countEntriesByType(fhirBundle, "Immunization")).isEqualTo(2);
    assertThat(countEntriesByType(fhirBundle, "Location")).isEqualTo(1);
    // verify full urls are "resource:N"
    for (int i = 0; i < fhirBundle.entry().size(); i++) {
      assertThat(fhirBundle.entry().get(i).fullUrl()).isEqualTo("resource:" + i);
    }
    // verify Immunization references
    long immValidated =
        fhirBundle.entry().stream()
            .filter(e -> e.resource() instanceof Immunization)
            .map(e -> (Immunization) e.resource())
            .peek(
                imm -> {
                  assertThat(imm.patient().reference()).isEqualTo("resource:0");
                  assertThat(imm.location().reference()).isEqualTo("resource:3");
                })
            .count();
    assertThat(immValidated).isEqualTo(2);
    // Verify Parameter resourceLinks
    assertThat(findResourceLinksFromParameters(result))
        .isEqualTo(
            List.of(
                resourceLink("resource:0", "http://dq.foo/r4/Patient/123"),
                resourceLink("resource:1", "http://dq.foo/r4/Immunization/imm-1-123"),
                resourceLink("resource:2", "http://dq.foo/r4/Immunization/imm-2-123"),
                resourceLink("resource:3", "http://dq.foo/r4/Location/loc-123")));
  }

  @Test
  void issueVc_EmptyParameters() {
    var controller = patientController(null);
    // Empty List
    assertThrows(
        Exceptions.BadRequest.class, () -> controller.issueVc("123", parametersEmpty(), ""));
    // null List
    assertThrows(
        Exceptions.BadRequest.class,
        () -> controller.issueVc("123", parametersEmpty().parameter(null), ""));
  }

  @Test
  void issueVc_invalidCredentialType() {
    var controller = patientController(null);
    assertThrows(
        Exceptions.InvalidCredentialType.class,
        () -> controller.issueVc("123", parametersWithCredentialType("NOPE"), ""));
  }

  @Test
  void issueVc_notFound() {
    var patientBundleResponse = new ResponseEntity<>(mockPatient("404"), HttpStatus.ACCEPTED);
    var controller = patientController(patientBundleResponse);
    assertThrows(
        Exceptions.NotFound.class, () -> controller.issueVc("404", parametersCovid19(), ""));
  }

  @Test
  void issueVc_unimplementedCredentialType() {
    var controller = patientController(null);
    assertThrows(
        Exceptions.NotImplemented.class,
        () ->
            controller.issueVc(
                "123", parametersWithCredentialType("https://smarthealth.cards#immunization"), ""));
  }

  private Parameters.Parameter resourceLink(String resource, String url) {
    return Parameters.Parameter.builder()
        .name("resourceLink")
        .part(
            List.of(
                Parameters.Parameter.builder().name("bundledResource").valueUri(resource).build(),
                Parameters.Parameter.builder().name("hostedResource").valueUri(url).build()))
        .build();
  }
}
