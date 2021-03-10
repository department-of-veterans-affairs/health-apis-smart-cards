package gov.va.api.health.smartcards;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.List;
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

  private static final Integer MODERNA_VACCINE_CODE = 207;

  private static final Integer PFIZER_VACCINE_CODE = 208;

  private static final Integer AZTRA_ZENACA_VACCINE_CODE = 210;

  private static final Integer JANSSEN_VACCINE_CODE = 212;

  final RestTemplate restTemplate;

  final LinkProperties linkProperties;

  @Override
  public Immunization.Bundle immunizationBundle(Patient patient, String authorization) {
    var entity = prepareHeaders(authorization);
    String url =
        String.format(
            "%s?patient=%s&_count=100",
            linkProperties.dataQueryR4ResourceUrl("Immunization"), patient.id());
    var immunizationBundle =
        restTemplate.exchange(url, HttpMethod.GET, entity, Immunization.Bundle.class).getBody();
    List<Integer> covidVaccines =
        List.of(
            MODERNA_VACCINE_CODE,
            PFIZER_VACCINE_CODE,
            AZTRA_ZENACA_VACCINE_CODE,
            JANSSEN_VACCINE_CODE);
    if (immunizationBundle != null && immunizationBundle.entry() != null) {
      immunizationBundle.entry(
          immunizationBundle.entry().stream()
              .filter(
                  entry -> {
                    var codingList =
                        entry.resource().vaccineCode().coding().stream()
                            .filter(
                                coding -> covidVaccines.contains(Integer.parseInt(coding.code())))
                            .collect(toList());
                    return !codingList.isEmpty();
                  })
              .collect(toList()));
      immunizationBundle.total((int) immunizationBundle.entry().stream().count());
      return immunizationBundle;
    } else {
      return null;
    }
  }

  @Override
  @SneakyThrows
  public Patient.Bundle patientBundle(String icn, String authorization) {
    var entity = prepareHeaders(authorization);
    String url = String.format("%s?_id=%s", linkProperties.dataQueryR4ResourceUrl("Patient"), icn);
    return restTemplate.exchange(url, HttpMethod.GET, entity, Patient.Bundle.class).getBody();
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
