package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.smartcards.Exceptions.FhirClientConnectionFailure;
import java.util.HashMap;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class DataQueryFhirClientTest {

  @Test
  void makesBadRequest() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    LinkProperties linkProperties = mock(LinkProperties.class);
    DataQueryFhirClient dataQueryFhirClient = new DataQueryFhirClient(restTemplate, linkProperties);
    var response = new ResponseEntity<>(Patient.Bundle.builder().build(), HttpStatus.BAD_REQUEST);
    when(restTemplate.exchange(
            any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(Patient.Bundle.class)))
        .thenReturn(response);
    assertThatThrownBy(
            () -> dataQueryFhirClient.patientBundle("123", new HashMap<String, String>()))
        .isInstanceOf(FhirClientConnectionFailure.class);
  }

  @Test
  @SneakyThrows
  void makesRequests() {
    RestTemplate restTemplate = mock(RestTemplate.class);
    LinkProperties linkProperties = mock(LinkProperties.class);
    DataQueryFhirClient dataQueryFhirClient = new DataQueryFhirClient(restTemplate, linkProperties);
    var response = new ResponseEntity<>(Patient.Bundle.builder().build(), HttpStatus.OK);
    when(restTemplate.exchange(
            any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(Patient.Bundle.class)))
        .thenReturn(response);
    assertThat(dataQueryFhirClient.patientBundle("123", new HashMap<String, String>())).isNotNull();
  }
}
