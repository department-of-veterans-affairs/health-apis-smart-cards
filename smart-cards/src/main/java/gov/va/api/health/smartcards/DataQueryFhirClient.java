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
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

@RequestMapping(
    value = "/r4",
    method = RequestMethod.POST,
    produces = {"application/json", "application/fhir+json"})
@Slf4j
@AllArgsConstructor
public class DataQueryFhirClient implements FhirClient {
  @NonNull @Autowired RestTemplate restTemplate;

  @NonNull @Autowired String url;

  @Override
  public Immunization.Bundle immunizationBundle(Patient patient) {
    return null;
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  /** Makes patient request to DQ. */
  @Override
  @SneakyThrows
  public Patient.Bundle patientBundle(String id) {
    var headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.set("Authorization", "Bearer " + System.getProperty("access-token", "unset"));
    var entity = new HttpEntity<>("body", headers);
    try {
      var returnedValue = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      log.info("STATUS CODE: {}", returnedValue.getStatusCode());
      ObjectMapper mapper = JacksonMapperConfig.createMapper();
      Patient.Bundle bundle = mapper.readValue(returnedValue.getBody(), Patient.Bundle.class);
      return bundle;
    } catch (Exception e) {
      log.info("Exception occurred. Get {} message: {}", url, e.getMessage());
      throw e;
    }
  }
}
