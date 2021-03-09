package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class DataQueryFhirClient implements FhirClient {
  final RestTemplate restTemplate;

  final LinkProperties linkProperties;

  @Override
  public Immunization.Bundle immunizationBundle(Patient patient, String authorization) {
    var dqHeaders = new HttpHeaders();
    if (authorization != null) {
      dqHeaders.set("Authorization", authorization);
    }
    dqHeaders.set("accept", "application/json");
    var entity = new HttpEntity<>(dqHeaders);
    String url =
        String.format(
            "%s?patient=%s", linkProperties.dataQueryR4ResourceUrl("Immunization"), patient.id());
    return restTemplate.exchange(url, HttpMethod.GET, entity, Immunization.Bundle.class).getBody();
  }

  @Override
  @SneakyThrows
  public Patient.Bundle patientBundle(String icn, String authorization) {
    var dqHeaders = new HttpHeaders();
    if (authorization != null) {
      dqHeaders.set("Authorization", authorization);
    }
    dqHeaders.set("accept", "application/json");
    var entity = new HttpEntity<>(dqHeaders);
    String url = String.format("%s?_id=%s", linkProperties.dataQueryR4ResourceUrl("Patient"), icn);
    return restTemplate.exchange(url, HttpMethod.GET, entity, Patient.Bundle.class).getBody();
  }
}
