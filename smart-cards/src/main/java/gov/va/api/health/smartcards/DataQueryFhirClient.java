package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Location.Bundle;
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
  public Immunization.Bundle immunizationBundle(Patient patient) {
    throw new Exceptions.NotImplemented("not-implemented");
  }

  @Override
  public Bundle locationBundle(String id) {
    throw new Exceptions.NotImplemented("not-implemented");
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
