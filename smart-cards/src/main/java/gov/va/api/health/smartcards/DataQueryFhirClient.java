package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Component
public class DataQueryFhirClient implements FhirClient {
  @NonNull RestTemplate restTemplate;

  @NonNull LinkProperties linkProperties;

  @Override
  public Immunization.Bundle immunizationBundle(Patient patient) {
    throw new Exceptions.NotImplemented("not-implemented");
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  /** Makes patient request to DQ. */
  @Override
  @SneakyThrows
  public Patient.Bundle patientBundle(
      String id, Map<String, String> headers) { // receive access token?
    var dqHeaders = new HttpHeaders();
    headers.forEach(dqHeaders::set);
    var entity = new HttpEntity<>(null, dqHeaders);

    String url = String.format("%s?_id=%s", linkProperties.dataQueryR4ResourceUrl("Patient"), id);

    var returnedValue = restTemplate.exchange(url, HttpMethod.GET, entity, Patient.Bundle.class);
    if (!returnedValue.getStatusCode().is2xxSuccessful()) {
      throw new Exceptions.FhirClientConnectionFailure(
          "Data Query Status Code is " + returnedValue.getStatusCode());
    }
    return returnedValue.getBody();
  }
}
