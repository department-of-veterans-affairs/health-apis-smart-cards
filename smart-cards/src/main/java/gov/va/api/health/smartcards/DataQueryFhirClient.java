package gov.va.api.health.smartcards;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.client.RestTemplate;

@Slf4j
@AllArgsConstructor
@Component
public class DataQueryFhirClient implements FhirClient {
  @Autowired RestTemplate restTemplate;

  @Autowired LinkProperties linkProperties;

  @Override
  public Immunization.Bundle immunizationBundle(Patient patient) {
    return null;
  }

  @Override
  @SneakyThrows
  public Patient.Bundle patientBundle(String icn, String key) {
    if (key == null) {
      key = System.getProperty("access-token", "unset");
    }
    var headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.set("Authorization", key);
    var entity = new HttpEntity<>(null, headers);

    String url = String.format("%s?_id=%s", linkProperties.dataQueryR4ResourceUrl("Patient"), icn);

    var returnedValue = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    log.info("STATUS CODE: {}", returnedValue.getStatusCode());
    if (!returnedValue.getStatusCode().is2xxSuccessful()) {
      throw new Exceptions.FhirClientConnectionFailure(
          String.format(
              "Data Query Status Code is %s\nData Query Body: %s",
              returnedValue.getStatusCode(),
              returnedValue.getBody())
      );
    }
    ObjectMapper mapper = JacksonMapperConfig.createMapper();
    Patient.Bundle bundle = mapper.readValue(returnedValue.getBody(), Patient.Bundle.class);
    return bundle;
  }
}
