package gov.va.api.health.smartcards;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class DataQueryFhirClientTest {
  @Test
  void invalidImmunizationBundleThrowsException() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    Immunization.Bundle bundle = Immunization.Bundle.builder().entry(emptyList()).build();
    var response = new ResponseEntity<>(bundle, HttpStatus.OK);
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Immunization.Bundle.class)))
        .thenReturn(response);
    DataQueryFhirClient dataQueryFhirClient =
        new DataQueryFhirClient(restTemplate, mock(LinkProperties.class));

    assertThat(dataQueryFhirClient.immunizationBundle("123", "")).isEqualTo(bundle);
  }

  @Test
  void makesRequests() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    var response = new ResponseEntity<>(Patient.Bundle.builder().build(), HttpStatus.OK);
    when(restTemplate.exchange(
            any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(Patient.Bundle.class)))
        .thenReturn(response);
    DataQueryFhirClient dataQueryFhirClient =
        new DataQueryFhirClient(restTemplate, mock(LinkProperties.class));
    assertThat(dataQueryFhirClient.patientBundle("123", ""))
        .isEqualTo(Patient.Bundle.builder().build());
  }
}
