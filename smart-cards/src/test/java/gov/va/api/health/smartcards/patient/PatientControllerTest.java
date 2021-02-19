package gov.va.api.health.smartcards.patient;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.resources.Parameters;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.smartcards.Exceptions;
import gov.va.api.health.smartcards.LinkProperties;
import gov.va.api.health.smartcards.MockFhirClient;
import gov.va.api.health.smartcards.R4MixedBundler;
import gov.va.api.health.smartcards.vc.VerifiableCredential;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DataBinder;

public class PatientControllerTest {
  public static final ObjectMapper MAPPER =
      JacksonConfig.createMapper().registerModule(new Resource.ResourceModule());

  private long countEntriesByType(MixedBundle bundle, String type) {
    checkNotNull(bundle);
    return bundle.entry().stream()
        .filter(e -> e.resource().getClass().getSimpleName().equals(type))
        .count();
  }

  @SneakyThrows
  private VerifiableCredential findVcFromParameters(Parameters parameters) {
    var maybeParam =
        parameters.parameter().stream()
            .filter(p -> "verifiableCredential".equals(p.name()))
            .findFirst();
    var parameter = maybeParam.orElseThrow();
    return MAPPER.readValue(parameter.valueString(), VerifiableCredential.class);
  }

  @Test
  void initDirectFieldAccess() {
    new PatientController(mock(MockFhirClient.class), mock(R4MixedBundler.class))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @Test
  public void issueVc() {
    var fhirClient = new MockFhirClient(mock(LinkProperties.class));
    var bundler = new R4MixedBundler();
    var controller = new PatientController(fhirClient, bundler);
    var result = controller.issueVc("123", parametersCovid19()).getBody();
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
    var fhirClient = new MockFhirClient(mock(LinkProperties.class));
    var bundler = new R4MixedBundler();
    var controller = new PatientController(fhirClient, bundler);
    // Empty List
    assertThrows(Exceptions.BadRequest.class, () -> controller.issueVc("123", parametersEmpty()));
    // null List
    assertThrows(
        Exceptions.BadRequest.class,
        () -> controller.issueVc("123", parametersEmpty().parameter(null)));
  }

  @Test
  public void issueVc_invalidCredentialType() {
    var fhirClient = new MockFhirClient(mock(LinkProperties.class));
    var bundler = new R4MixedBundler();
    var controller = new PatientController(fhirClient, bundler);
    assertThrows(
        Exceptions.InvalidCredentialType.class,
        () -> controller.issueVc("123", parametersWithCredentialType("NOPE")));
  }

  @Test
  public void issueVc_notFound() {
    var fhirClient = new MockFhirClient(mock(LinkProperties.class));
    var bundler = new R4MixedBundler();
    var controller = new PatientController(fhirClient, bundler);
    assertThrows(Exceptions.NotFound.class, () -> controller.issueVc("404", parametersCovid19()));
  }

  @Test
  public void issueVc_unimplementedCredentialType() {
    var fhirClient = new MockFhirClient(mock(LinkProperties.class));
    var bundler = new R4MixedBundler();
    var controller = new PatientController(fhirClient, bundler);
    assertThrows(
        Exceptions.NotImplemented.class,
        () ->
            controller.issueVc(
                "123", parametersWithCredentialType("https://smarthealth.cards#immunization")));
  }

  private Parameters parametersCovid19() {
    return parametersWithCredentialType("https://smarthealth.cards#covid19");
  }

  private Parameters parametersEmpty() {
    return Parameters.builder().parameter(List.of()).build();
  }

  private Parameters parametersWithCredentialType(String... credentialType) {
    return Parameters.builder()
        .parameter(
            Arrays.stream(credentialType)
                .map(c -> Parameters.Parameter.builder().name("credentialType").valueUri(c).build())
                .collect(toList()))
        .build();
  }
}
