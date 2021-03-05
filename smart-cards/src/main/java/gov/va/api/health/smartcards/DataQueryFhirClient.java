package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Component
public class DataQueryFhirClient implements FhirClient {

  final RestTemplate restTemplate;

  final LinkProperties linkProperties;

  @Override
  public Immunization.Bundle immunizationBundle(Patient patient) {
    throw new Exceptions.NotImplemented("not-implemented");
  }

  @Override
  @SneakyThrows
  public Patient.Bundle patientBundle(String icn, Map<String, String> headers) {
    var dqHeaders = new HttpHeaders();
    headers.forEach(dqHeaders::set);
    var entity = new HttpEntity<>(dqHeaders);
    String url = String.format("%s?_id=%s", linkProperties.dataQueryR4ResourceUrl("Patient"), icn);
    var returnedValue = restTemplate.exchange(url, HttpMethod.GET, entity, Patient.Bundle.class);
    if (!returnedValue.getStatusCode().is2xxSuccessful()) {
      throw new Exceptions.FhirClientConnectionFailure(
          String.format(
              "Data Query Status Code is %s%nData Query Body: %s",
              returnedValue.getStatusCode(), returnedValue.getBody()));
    }
    return returnedValue.getBody();
  }
}
