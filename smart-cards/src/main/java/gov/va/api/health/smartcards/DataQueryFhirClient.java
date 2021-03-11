package gov.va.api.health.smartcards;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.smartcards.Exceptions.FhirConnectionFailure;
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
  // Vaccine Code reference here:
  // https://www.cdc.gov/vaccines/programs/iis/COVID-19-related-codes.html
  private static final List<String> COVID19_VACCINE_CODES = List.of("207", "208", "210", "212");

  final RestTemplate restTemplate;

  final LinkProperties linkProperties;

  private <T> ResponseEntity<T> doGet(String url, String authorization, Class<T> responseType) {
    HttpEntity<HttpHeaders> entity = prepareHeaders(authorization);
    return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
  }

  @Override
  public Immunization.Bundle immunizationBundle(Patient patient, String authorization) {
    String url =
        String.format(
            "%s?patient=%s&_count=100",
            linkProperties.dataQueryR4ResourceUrl("Immunization"), patient.id());
    var immunizationBundle = doGet(url, authorization, Immunization.Bundle.class).getBody();
    if (immunizationBundle == null || immunizationBundle.entry() == null) {
      throw new FhirConnectionFailure(
          String.format("Received null response body for Immunization search"));
    }
    immunizationBundle.entry(
        immunizationBundle.entry().stream()
            .filter(
                entry -> {
                  var codingList =
                      entry.resource().vaccineCode().coding().stream()
                          .filter(coding -> COVID19_VACCINE_CODES.contains(coding.code()))
                          .collect(toList());
                  return !codingList.isEmpty();
                })
            .collect(toList()));
    immunizationBundle.total(immunizationBundle.entry().size());
    return immunizationBundle;
  }

  @Override
  @SneakyThrows
  public Patient.Bundle patientBundle(String icn, String authorization) {
    String url = String.format("%s?_id=%s", linkProperties.dataQueryR4ResourceUrl("Patient"), icn);
    return doGet(url, authorization, Patient.Bundle.class).getBody();
  }

  private HttpEntity<HttpHeaders> prepareHeaders(String authorization) {
    var dqHeaders = new HttpHeaders();
    if (authorization != null) {
      dqHeaders.set("Authorization", authorization);
    }
    dqHeaders.set("accept", "application/json");
    return new HttpEntity<>(dqHeaders);
  }
}
