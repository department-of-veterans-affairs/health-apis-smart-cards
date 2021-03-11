package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.smartcards.Exceptions.FhirConnectionFailure;
import java.util.List;
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
    var response = new ResponseEntity<>(Immunization.Bundle.builder().build(), HttpStatus.OK);
    when(restTemplate.exchange(
            any(String.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(Immunization.Bundle.class)))
        .thenReturn(response);
    DataQueryFhirClient dataQueryFhirClient =
        new DataQueryFhirClient(restTemplate, mock(LinkProperties.class));
    assertThatThrownBy(
            () ->
                dataQueryFhirClient.immunizationBundle(
                    Patient.builder().id("123").name(List.of(HumanName.builder().build())).build(),
                    ""))
        .isInstanceOf(FhirConnectionFailure.class);
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
