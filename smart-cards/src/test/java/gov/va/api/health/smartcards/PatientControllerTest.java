package gov.va.api.health.smartcards;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.Parameters;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.web.client.RestTemplate;

public class PatientControllerTest {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  private static final JwksProperties JWKS_PROPERTIES = JwsHelpers.jwksProperties("123");

  private static final MockResourceSamples SAMPLES =
      MockResourceSamples.builder().linkProperties(linkProperties()).build();

  static Stream<Arguments> _credentialTypes() {
    // HEALTH_CARD, IMMUNIZATION, LABORATORY , COVID_19
    boolean o = false;
    boolean l = true;
    return Stream.of(
        arguments(o, o, o, o, Exceptions.BadRequest.class),
        arguments(o, o, o, l, null),
        arguments(o, o, l, o, Exceptions.NotImplemented.class),
        arguments(o, o, l, l, Exceptions.NotImplemented.class),
        arguments(o, l, o, o, Exceptions.NotImplemented.class),
        arguments(o, l, o, l, null),
        arguments(o, l, l, o, Exceptions.NotImplemented.class),
        arguments(o, l, l, l, Exceptions.NotImplemented.class),
        arguments(l, o, o, o, Exceptions.BadRequest.class),
        arguments(l, o, o, l, null),
        arguments(l, o, l, o, Exceptions.NotImplemented.class),
        arguments(l, o, l, l, Exceptions.NotImplemented.class),
        arguments(l, l, o, o, Exceptions.NotImplemented.class),
        arguments(l, l, o, l, null),
        arguments(l, l, l, o, Exceptions.NotImplemented.class),
        arguments(l, l, l, l, Exceptions.NotImplemented.class));
  }

  @BeforeAll
  static void _init() {
    Patient.IDENTIFIER_MIN_SIZE.set(0);
  }

  private static long countEntriesByType(MixedBundle bundle, String type) {
    checkNotNull(bundle);
    return bundle.entry().stream()
        .filter(e -> e.resource().getClass().getSimpleName().equals(type))
        .count();
  }

  private static Parameters doHealthCardsIssue(String vcJws, String vcCompress) {
    var patientBundleResponse =
        new ResponseEntity<>(SAMPLES.patientBundle("123"), HttpStatus.ACCEPTED);
    var immunizationBundleResponse =
        new ResponseEntity<>(SAMPLES.immunizationBundle("123"), HttpStatus.ACCEPTED);
    var locationResponse = new ResponseEntity<>(SAMPLES.location("loc-1"), HttpStatus.ACCEPTED);
    var controller =
        patientController(patientBundleResponse, immunizationBundleResponse, locationResponse);
    var result =
        controller.healthCardsIssue("123", parametersCovid19(), "", vcJws, vcCompress).getBody();
    assertNotNull(result);
    return result;
  }

  private static List<Parameters.Parameter> findResourceLinksFromParameters(Parameters parameters) {
    return parameters.parameter().stream()
        .filter(p -> "resourceLink".equals(p.name()))
        .collect(toList());
  }

  private static String findVcFromParameters(Parameters parameters) {
    var maybeParam =
        parameters.parameter().stream()
            .filter(p -> "verifiableCredential".equals(p.name()))
            .findFirst();
    var parameter = maybeParam.orElseThrow();
    return parameter.valueString();
  }

  @SneakyThrows
  private static PayloadClaimsWrapper getPayloadFromJws(String jws, boolean compressed) {
    var jwsObject = JwsHelpers.parse(jws);
    var payloadJson =
        compressed
            ? JwsHelpers.decompress(jwsObject.getPayload().toBytes())
            : jwsObject.getPayload().toString();
    return MAPPER.readValue(payloadJson, PayloadClaimsWrapper.class);
  }

  private static LinkProperties linkProperties() {
    return LinkProperties.builder()
        .dqInternalUrl("http://dq.foo")
        .dqInternalR4BasePath("r4")
        .baseUrl("http://sc.bar")
        .r4BasePath("r4")
        .build();
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
            Stream.of(credentialType)
                .map(c -> Parameters.Parameter.builder().name("credentialType").valueUri(c).build())
                .collect(toList()))
        .build();
  }

  private static PatientController patientController(
      ResponseEntity<Patient.Bundle> patientBundleResponse,
      ResponseEntity<Immunization.Bundle> immunizationBundleResponse,
      ResponseEntity<Location> locationResponse) {
    var mockRestTemplate = mock(RestTemplate.class);
    if (patientBundleResponse != null) {
      when(mockRestTemplate.exchange(
              anyString(), any(HttpMethod.class), any(), same(Patient.Bundle.class)))
          .thenReturn(patientBundleResponse);
    }
    if (immunizationBundleResponse != null) {
      when(mockRestTemplate.exchange(
              anyString(), any(HttpMethod.class), any(), same(Immunization.Bundle.class)))
          .thenReturn(immunizationBundleResponse);
    }
    if (locationResponse != null) {
      when(mockRestTemplate.exchange(
              anyString(), any(HttpMethod.class), any(), same(Location.class)))
          .thenReturn(locationResponse);
    }
    var fhirClient = new DataQueryFhirClient(mockRestTemplate, linkProperties());
    var signer = new PayloadSigner(JWKS_PROPERTIES, linkProperties());
    return new PatientController(fhirClient, signer);
  }

  private static Parameters.Parameter resourceLink(String resource, String url) {
    return Parameters.Parameter.builder()
        .name("resourceLink")
        .part(
            List.of(
                Parameters.Parameter.builder().name("bundledResource").valueUri(resource).build(),
                Parameters.Parameter.builder().name("hostedResource").valueUri(url).build()))
        .build();
  }

  @ParameterizedTest
  @MethodSource(value = "_credentialTypes")
  void credentialTypes(
      boolean healthCard,
      boolean immunization,
      boolean laboratory,
      boolean covid19,
      Class<? extends Exception> exClass) {
    List<String> types = new ArrayList<>();
    if (healthCard) {
      types.add("https://smarthealth.cards#health-card");
    }
    if (immunization) {
      types.add("https://smarthealth.cards#immunization");
    }
    if (laboratory) {
      types.add("https://smarthealth.cards#laboratory");
    }
    if (covid19) {
      types.add("https://smarthealth.cards#covid19");
    }
    if (exClass == null) {
      Set<CredentialType> credentials =
          PatientController.credentialTypes(
              parametersWithCredentialType(types.toArray(new String[0])));
      assertThat(credentials)
          .containsExactlyInAnyOrder(
              CredentialType.HEALTH_CARD, CredentialType.IMMUNIZATION, CredentialType.COVID_19);
    } else {
      assertThrows(
          exClass,
          () ->
              PatientController.credentialTypes(
                  parametersWithCredentialType(types.toArray(new String[0]))));
    }
  }

  @Test
  @SneakyThrows
  void healthCardsIssue() {
    var result = doHealthCardsIssue("false", "false");
    var vcJson = findVcFromParameters(result);
    var vc = MAPPER.readValue(vcJson, VerifiableCredential.class);
    assertThat(vc.context()).isEqualTo(List.of("https://www.w3.org/2018/credentials/v1"));
    assertThat(vc.type()).contains("VerifiableCredential", "https://smarthealth.cards#covid19");
    var fhirBundle = vc.credentialSubject().fhirBundle();
    assertThat(fhirBundle.entry()).hasSize(3);
    assertThat(countEntriesByType(fhirBundle, "Patient")).isEqualTo(1);
    assertThat(countEntriesByType(fhirBundle, "Immunization")).isEqualTo(2);
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
                })
            .count();
    assertThat(immValidated).isEqualTo(2);
    // Verify Parameter resourceLinks
    assertThat(findResourceLinksFromParameters(result))
        .isEqualTo(
            List.of(
                resourceLink("resource:0", "http://dq.foo/r4/Patient/123"),
                resourceLink("resource:1", "http://dq.foo/r4/Immunization/imm-1-123"),
                resourceLink("resource:2", "http://dq.foo/r4/Immunization/imm-2-123")));
  }

  @Test
  void healthCardsIssue_emptyParameters() {
    var controller = patientController(null, null, null);
    // Empty List
    assertThrows(
        Exceptions.BadRequest.class,
        () -> controller.healthCardsIssue("123", parametersEmpty(), "", "", ""));
    // null List
    assertThrows(
        Exceptions.BadRequest.class,
        () -> controller.healthCardsIssue("123", parametersEmpty().parameter(null), "", "", ""));
  }

  @Test
  void healthCardsIssue_invalidCredentialType() {
    var controller = patientController(null, null, null);
    assertThrows(
        Exceptions.InvalidCredentialType.class,
        () -> controller.healthCardsIssue("123", parametersWithCredentialType("NOPE"), "", "", ""));
  }

  @Test
  void healthCardsIssue_moreGranularCredentialType() {
    var controller = patientController(null, null, null);
    assertThrows(
        Exceptions.BadRequest.class,
        () ->
            controller.healthCardsIssue(
                "123",
                parametersWithCredentialType("https://smarthealth.cards#health-card"),
                "",
                "",
                ""));
    assertThrows(
        Exceptions.NotImplemented.class,
        () ->
            controller.healthCardsIssue(
                "123",
                parametersWithCredentialType("https://smarthealth.cards#immunization"),
                "",
                "",
                ""));
  }

  @Test
  void healthCardsIssue_noImmunizationsOnRecord() {
    var patientBundleResponse =
        new ResponseEntity<>(SAMPLES.patientBundle("123"), HttpStatus.ACCEPTED);
    var immunizationBundleResponse =
        new ResponseEntity<>(SAMPLES.immunizationEmpty("123"), HttpStatus.ACCEPTED);
    var controller = patientController(patientBundleResponse, immunizationBundleResponse, null);
    var result = controller.healthCardsIssue("123", parametersCovid19(), "", "", "").getBody();
    assertNotNull(result);
    assertThat(result).isEqualTo(Parameters.builder().parameter(List.of()).build());
  }

  @Test
  void healthCardsIssue_noSuchPatient() {
    var patientBundleResponse =
        new ResponseEntity<>(SAMPLES.patientEmpty("123"), HttpStatus.ACCEPTED);
    var controller = patientController(patientBundleResponse, null, null);
    assertThrows(
        Exceptions.NotFound.class,
        () -> controller.healthCardsIssue("123", parametersCovid19(), "", "", "").getBody());
  }

  /**
   * This test only verifies that the signed and compressed response is consistent with the unsigned
   * version. The VerifiableCredential structure is tested through the `healthCardsIssue` test.
   */
  @Test
  @SneakyThrows
  void healthCardsIssue_signedAndCompressed() {
    var result = doHealthCardsIssue("", "");
    var jws = findVcFromParameters(result);
    assertThat(JwsHelpers.verify(jws, JWKS_PROPERTIES.currentPublicJwk())).isTrue();
    var payloadClaims = getPayloadFromJws(jws, true);
    var resultNotSigned = doHealthCardsIssue("false", "false");
    var vc = MAPPER.readValue(findVcFromParameters(resultNotSigned), VerifiableCredential.class);
    assertThat(payloadClaims.verifiableCredential()).isEqualTo(vc);
  }

  /**
   * This test only verifies that the signed and uncompressed response is consistent with the
   * unsigned version. The VerifiableCredential structure is tested through the `healthCardsIssue`
   * test.
   */
  @Test
  @SneakyThrows
  void healthCardsIssue_signedButNotCompressed() {
    var resultSigned = doHealthCardsIssue("", "false");
    var jws = findVcFromParameters(resultSigned);
    assertThat(JwsHelpers.verify(jws, JWKS_PROPERTIES.currentPublicJwk())).isTrue();
    var payloadClaims = getPayloadFromJws(jws, false);
    var resultNotSigned = doHealthCardsIssue("false", "false");
    var vc = MAPPER.readValue(findVcFromParameters(resultNotSigned), VerifiableCredential.class);
    assertThat(payloadClaims.verifiableCredential()).isEqualTo(vc);
  }

  @Test
  void healthCardsIssue_unimplementedCredentialType() {
    var controller = patientController(null, null, null);
    assertThrows(
        Exceptions.NotImplemented.class,
        () ->
            controller.healthCardsIssue(
                "123",
                parametersWithCredentialType("https://smarthealth.cards#laboratory"),
                "",
                "",
                ""));
  }

  @Test
  void initDirectFieldAccess() {
    new PatientController(
            new DataQueryFhirClient(mock(RestTemplate.class), linkProperties()),
            new PayloadSigner(JWKS_PROPERTIES, linkProperties()))
        .initDirectFieldAccess(mock(DataBinder.class));
  }
}
