package gov.va.api.health.smartcards.patient;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.smartcards.Exceptions;
import gov.va.api.health.smartcards.LinkProperties;
import gov.va.api.health.smartcards.MockFhirClient;
import gov.va.api.health.smartcards.R4MixedBundler;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DataBinder;

public class PatientControllerTest {
  private long countEntriesByType(MixedBundle bundle, String type) {
    checkNotNull(bundle);
    return bundle.entry().stream()
        .filter(e -> e.resource().getClass().getSimpleName().equals(type))
        .count();
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
    var result = controller.issueVc("123").getBody();
    assertNotNull(result);
    assertThat(result.context()).isEqualTo(List.of("https://www.w3.org/2018/credentials/v1"));
    assertThat(result.type()).contains("VerifiableCredential", "https://smarthealth.cards#covid19");
    var fhirBundle = result.credentialSubject().fhirBundle();
    assertThat(fhirBundle.entry()).hasSize(fhirBundle.total());
    assertThat(countEntriesByType(fhirBundle, "Patient")).isEqualTo(1);
    assertThat(countEntriesByType(fhirBundle, "Immunization")).isEqualTo(2);
  }

  @Test
  public void issueVc_notFound() {
    var fhirClient = new MockFhirClient(mock(LinkProperties.class));
    var bundler = new R4MixedBundler();
    var controller = new PatientController(fhirClient, bundler);
    assertThrows(Exceptions.NotFound.class, () -> controller.issueVc("404"));
  }
}
