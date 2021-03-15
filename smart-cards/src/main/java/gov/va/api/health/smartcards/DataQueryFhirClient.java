package gov.va.api.health.smartcards;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class DataQueryFhirClient implements FhirClient {
  // https://www.cdc.gov/vaccines/programs/iis/COVID-19-related-codes.html
  private static final List<String> COVID19_VACCINE_CODES = List.of("207", "208", "210", "212");

  final RestTemplate restTemplate;

  final LinkProperties linkProperties;

  private static HttpEntity<HttpHeaders> prepareHeaders(String authorization) {
    var dqHeaders = new HttpHeaders();
    if (authorization != null) {
      dqHeaders.set("Authorization", authorization);
    }
    dqHeaders.set("accept", "application/json");
    return new HttpEntity<>(dqHeaders);
  }

  private <T> ResponseEntity<T> doGet(String url, String authorization, Class<T> responseType) {
    HttpEntity<HttpHeaders> entity = prepareHeaders(authorization);
    return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
  }

  @Override
  public Immunization.Bundle immunizationBundle(String icn, String authorization) {
    String url =
        String.format(
            "%s?patient=%s&_count=100", linkProperties.dataQueryR4ResourceUrl("Immunization"), icn);
    var immunizationBundle = doGet(url, authorization, Immunization.Bundle.class).getBody();
    if (immunizationBundle == null) {
      return null;
    }
    // filter completed COVID-19 immunizations
    immunizationBundle.entry(
        immunizationBundle.entry().stream()
            .filter(entry -> entry.resource().status() == Immunization.Status.completed)
            .filter(
                entry ->
                    entry.resource().vaccineCode().coding().stream()
                        .anyMatch(coding -> COVID19_VACCINE_CODES.contains(coding.code())))
            .collect(toList()));
    immunizationBundle.total(immunizationBundle.entry().size());
    return immunizationBundle;
  }

  @Override
  public Location location(String id, String authorization) {
    String url = String.format("%s/%s", linkProperties.dataQueryR4ResourceUrl("Location"), id);
    return doGet(url, authorization, Location.class).getBody();
  }

  @Override
  @SneakyThrows
  public Patient.Bundle patientBundle(String icn, String authorization) {
    String url = String.format("%s?_id=%s", linkProperties.dataQueryR4ResourceUrl("Patient"), icn);
    return doGet(url, authorization, Patient.Bundle.class).getBody();
  }
}
