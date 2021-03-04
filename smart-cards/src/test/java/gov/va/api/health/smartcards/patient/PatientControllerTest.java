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
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.bundle.MixedBundle;
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

  private static long countEntriesByType(MixedBundle bundle, String type) {
    checkNotNull(bundle);
    return bundle.entry().stream()
        .filter(e -> e.resource().getClass().getSimpleName().equals(type))
        .count();
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

  private PatientController createMockController(ResponseEntity<String> patientBundleResponse) {
    var mockRestTemplate = mock(RestTemplate.class);
    when(mockRestTemplate.exchange(
        anyString(), any(HttpMethod.class), any(), ArgumentMatchers.<Class<String>>any()))
        .thenReturn(patientBundleResponse);
    var mockFhirClient = new MockFhirClient(mock(LinkProperties.class));
    var fhirClient = new DataQueryFhirClient(mockRestTemplate, mock(LinkProperties.class));
    var bundler = new R4MixedBundler();
    return new PatientController(fhirClient, mockFhirClient, bundler);
  }

  private PatientController createEmptyMockController() {
    var fhirClient = new DataQueryFhirClient(mock(RestTemplate.class), mock(LinkProperties.class));
    var mockFhirClient = new MockFhirClient(mock(LinkProperties.class));
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
  public void issueVc() {
    var patientBundleResponse = new ResponseEntity<String>(mockPatient("123"), HttpStatus.ACCEPTED);
    var controller = createMockController(patientBundleResponse);
    var result = controller.issueVc("123", parametersCovid19(), "someKey").getBody();
    assertNotNull(result);
    var vc = findVcFromParameters(result);
    assertThat(vc.context()).isEqualTo(List.of("https://www.w3.org/2018/credentials/v1"));
    assertThat(vc.type()).contains("VerifiableCredential", "https://smarthealth.cards#covid19");
    var fhirBundle = vc.credentialSubject().fhirBundle();
    assertThat(fhirBundle.entry()).hasSize(fhirBundle.total());
    assertThat(countEntriesByType(fhirBundle, "Patient")).isEqualTo(1);
    assertThat(countEntriesByType(fhirBundle, "Immunization")).isEqualTo(2);
  }

  @Test
  public void issueVc_EmptyParameters() {
    var controller = createEmptyMockController();
    // Empty List
    assertThrows(
        Exceptions.BadRequest.class, () -> controller.issueVc("123", parametersEmpty(), "someKey"));
    // null List
    assertThrows(
        Exceptions.BadRequest.class,
        () -> controller.issueVc("123", parametersEmpty().parameter(null), "someKey"));
  }

  @Test
  public void issueVc_invalidCredentialType() {
    var controller = createEmptyMockController();
    assertThrows(
        Exceptions.InvalidCredentialType.class,
        () -> controller.issueVc("123", parametersWithCredentialType("NOPE"), "someKey"));
  }

  @Test
  public void issueVc_notFound() {
    var patientBundleResponse = new ResponseEntity<String>(mockPatient("404"), HttpStatus.ACCEPTED);
    var controller = createMockController(patientBundleResponse);
    assertThrows(
        Exceptions.NotFound.class, () -> controller.issueVc("404", parametersCovid19(), "someKey"));
  }

  @Test
  public void issueVc_unimplementedCredentialType() {
    var controller = createMockController(null);
    assertThrows(
        Exceptions.NotImplemented.class,
        () ->
            controller.issueVc(
                "123",
                parametersWithCredentialType("https://smarthealth.cards#immunization"),
                "someKey"));
  }

  @SneakyThrows
  String mockPatient(String id) {
    var mapper = JacksonConfig.createMapper();
    var mockFhirClient = new MockFhirClient(mock(LinkProperties.class));
    var patient = mockFhirClient.patientBundle(id, "someKey");
    return mapper.writeValueAsString(patient);
  }
}
