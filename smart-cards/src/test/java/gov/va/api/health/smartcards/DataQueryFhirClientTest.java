package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Patient;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class DataQueryFhirClientTest {

  @SneakyThrows
  public static String contentOf(String resource) {
    return Resources.toString(
        DataQueryFhirClientTest.class.getResource(resource), StandardCharsets.UTF_8);
  }

  @Test
  @SneakyThrows
  void makesRequests() {
    var mapper = JacksonConfig.createMapper();
    RestTemplate restTemplate = mock(RestTemplate.class);
    LinkProperties linkProperties = mock(LinkProperties.class);
    DataQueryFhirClient dataQueryFhirClient = new DataQueryFhirClient(restTemplate, linkProperties);
    var response =
        new ResponseEntity<>(
            mapper.readValue(contentOf("/patient-bundle.json"), Patient.Bundle.class),
            HttpStatus.OK);
    when(restTemplate.exchange(
            any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(Patient.Bundle.class)))
        .thenReturn(response);
    assertThat(dataQueryFhirClient.patientBundle("123", new HashMap<String, String>())).isNotNull();
  }
}
