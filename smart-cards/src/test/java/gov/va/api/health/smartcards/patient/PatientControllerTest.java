package gov.va.api.health.smartcards.patient;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.datatypes.Annotation;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Parameters;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.smartcards.DataQueryFhirClient;
import gov.va.api.health.smartcards.Exceptions;
import gov.va.api.health.smartcards.JacksonMapperConfig;
import gov.va.api.health.smartcards.LinkProperties;
import gov.va.api.health.smartcards.R4MixedBundler;
import gov.va.api.health.smartcards.vc.VerifiableCredential;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.web.client.RestTemplate;

public class PatientControllerTest {
  public static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  private static long countEntriesByType(MixedBundle bundle, String type) {
    checkNotNull(bundle);
    return bundle.entry().stream()
        .filter(e -> e.resource().getClass().getSimpleName().equals(type))
        .count();
  }

  @SneakyThrows
  private static VerifiableCredential findVcFromParameters(Parameters parameters) {
    var maybeParam =
        parameters.parameter().stream()
            .filter(p -> "verifiableCredential".equals(p.name()))
            .findFirst();
    var parameter = maybeParam.orElseThrow();
    return MAPPER.readValue(parameter.valueString(), VerifiableCredential.class);
  }

  static Immunization.Bundle immunizationBundle(String icn) {
    LinkProperties linkProperties = mock(LinkProperties.class);
    var patient = Patient.builder().id(icn).name(List.of(HumanName.builder().build())).build();
    String vaccineSystem = "http://hl7.org/fhir/sid/cvx";
    List<Immunization> immunizations =
        List.of(
            Immunization.builder()
                .resourceType("Immunization")
                .id(String.format("imm-1-%s", patient.id()))
                .status(Immunization.Status.completed)
                .vaccineCode(
                    CodeableConcept.builder()
                        .coding(List.of(Coding.builder().system(vaccineSystem).code("207").build()))
                        .text("COVID-19, mRNA, LNP-S, PF, 100 mcg/ 0.5 mL dose")
                        .build())
                .patient(
                    Reference.builder()
                        .reference(linkProperties.dataQueryR4ReadUrl(patient))
                        .display(patient.name().stream().findFirst().get().text())
                        .build())
                .occurrenceDateTime("2020-12-18T12:24:55Z")
                .primarySource(true)
                .location(
                    Reference.builder()
                        .reference(
                            String.format(
                                "%s/loc-%s",
                                linkProperties.dataQueryR4ResourceUrl("Location"), patient.id()))
                        .display("Location for " + patient.id())
                        .build())
                .note(
                    List.of(
                        Annotation.builder()
                            .text(
                                "Dose #1 of 2 of COVID-19, mRNA, LNP-S, PF, 100 mcg/ 0.5 mL dose "
                                    + "vaccine administered.")
                            .build()))
                .build(),
            Immunization.builder()
                .resourceType("Immunization")
                .id(String.format("imm-2-%s", patient.id()))
                .status(Immunization.Status.completed)
                .vaccineCode(
                    CodeableConcept.builder()
                        .coding(List.of(Coding.builder().system(vaccineSystem).code("207").build()))
                        .text("COVID-19, mRNA, LNP-S, PF, 100 mcg/ 0.5 mL dose")
                        .build())
                .patient(
                    Reference.builder()
                        .reference(linkProperties.dataQueryR4ReadUrl(patient))
                        .display(patient.name().stream().findFirst().get().text())
                        .build())
                .occurrenceDateTime("2021-01-14T09:30:21Z")
                .primarySource(true)
                .location(
                    Reference.builder()
                        .reference(
                            String.format(
                                "%s/loc-%s",
                                linkProperties.dataQueryR4ResourceUrl("Location"), patient.id()))
                        .display("Location for " + patient.id())
                        .build())
                .note(
                    List.of(
                        Annotation.builder()
                            .text(
                                "Dose #2 of 2 of COVID-19, mRNA, LNP-S, PF, 100 mcg/ 0.5 mL dose "
                                    + "vaccine administered.")
                            .build()))
                .build(), // 'not_done' Immunization to verify filters
            Immunization.builder()
                .resourceType("Immunization")
                .id(String.format("imm-3-%s", patient.id()))
                .status(Immunization.Status.not_done)
                .vaccineCode(
                    CodeableConcept.builder()
                        .coding(List.of(Coding.builder().system(vaccineSystem).code("207").build()))
                        .text("COVID-19, mRNA, LNP-S, PF, 100 mcg/ 0.5 mL dose")
                        .build())
                .patient(
                    Reference.builder()
                        .reference(linkProperties.dataQueryR4ReadUrl(patient))
                        .display(patient.name().stream().findFirst().get().text())
                        .build())
                .occurrenceDateTime("2021-01-18T09:30:21Z")
                .primarySource(true)
                .location(
                    Reference.builder()
                        .reference(
                            String.format(
                                "%s/loc-%s",
                                linkProperties.dataQueryR4ResourceUrl("Location"), patient.id()))
                        .display("Location for " + patient.id())
                        .build())
                .build());
    return Immunization.Bundle.builder()
        .type(AbstractBundle.BundleType.searchset)
        .link(
            List.of(
                BundleLink.builder()
                    .relation(BundleLink.LinkRelation.self)
                    .url(
                        String.format(
                            "%s?patient=%s",
                            linkProperties.dataQueryR4ResourceUrl("Immunization"), patient.id()))
                    .build()))
        .total(immunizations.size())
        .entry(
            immunizations.stream()
                .map(
                    t ->
                        Immunization.Entry.builder()
                            .resource(t)
                            .fullUrl(linkProperties.dataQueryR4ReadUrl(t))
                            .search(
                                AbstractEntry.Search.builder()
                                    .mode(AbstractEntry.SearchMode.match)
                                    .build())
                            .build())
                .collect(toList()))
        .build();
  }

  @BeforeAll
  static void init() {
    Patient.IDENTIFIER_MIN_SIZE.set(0);
  }

  private static Parameters parametersCovid19() {
    return parametersWithCredentialType("https://smarthealth.cards#covid19");
  }

  private static Parameters parametersEmpty() {
    return Parameters.builder().parameter(List.of()).build();
  }

  private static Parameters parametersWithCredentialType(String... credentialType) {
    return Parameters.builder()
        .parameter(
            Arrays.stream(credentialType)
                .map(c -> Parameters.Parameter.builder().name("credentialType").valueUri(c).build())
                .collect(toList()))
        .build();
  }

  static Patient.Bundle patientBundle(String id) {
    LinkProperties linkProperties = mock(LinkProperties.class);
    String firstName = "Joe" + id;
    String lastName = "Doe" + id;
    Patient patient =
        Patient.builder()
            .resourceType("Patient")
            .id(id)
            .identifier(
                List.of(
                    Identifier.builder().id(id).use(Identifier.IdentifierUse.temp).build(),
                    Identifier.builder()
                        .use(Identifier.IdentifierUse.usual)
                        .type(
                            CodeableConcept.builder()
                                .coding(
                                    List.of(
                                        Coding.builder()
                                            .system("http://hl7.org/fhir/v2/0203")
                                            .code("MR")
                                            .build()))
                                .build())
                        .system("http://va.gov/mpi")
                        .value(id)
                        .assigner(Reference.builder().display("Master Patient Index").build())
                        .build()))
            .active(true)
            .name(
                List.of(
                    HumanName.builder()
                        .use(HumanName.NameUse.anonymous)
                        .text(String.format("%s %s", firstName, lastName))
                        .family(lastName)
                        .given(List.of(firstName))
                        .build()))
            .gender(Patient.Gender.unknown)
            .birthDate("1955-01-01")
            .deceasedBoolean(false)
            .build();
    return Patient.Bundle.builder()
        .type(AbstractBundle.BundleType.searchset)
        .link(
            List.of(
                BundleLink.builder()
                    .relation(BundleLink.LinkRelation.self)
                    .url(
                        String.format(
                            "%s?_id=%s", linkProperties.dataQueryR4ResourceUrl("Patient"), id))
                    .build()))
        .total(1)
        .entry(
            List.of(
                Patient.Entry.builder()
                    .resource(patient)
                    .fullUrl(linkProperties.dataQueryR4ReadUrl(patient))
                    .search(
                        AbstractEntry.Search.builder().mode(AbstractEntry.SearchMode.match).build())
                    .build()))
        .build();
  }

  private static PatientController patientController(
      ResponseEntity<Patient.Bundle> patientBundleResponse,
      ResponseEntity<Immunization.Bundle> immunizationBundleResponse) {
    var mockRestTemplate = mock(RestTemplate.class);
    if (patientBundleResponse != null) {
      when(mockRestTemplate.exchange(
              anyString(), any(HttpMethod.class), any(), same(Patient.Bundle.class)))
          .thenReturn(patientBundleResponse);
    }
    if (immunizationBundleResponse != null) {
      when(mockRestTemplate.exchange(
              anyString(), any(HttpMethod.class), any(), same(Immunization.Bundle.class)))
          .thenReturn(immunizationBundleResponse);
    }
    var fhirClient = new DataQueryFhirClient(mockRestTemplate, mock(LinkProperties.class));
    var bundler = new R4MixedBundler();
    return new PatientController(fhirClient, bundler);
  }

  @Test
  void initDirectFieldAccess() {
    new PatientController(mock(DataQueryFhirClient.class), mock(R4MixedBundler.class))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @Test
  void issueVc() {
    var patientBundleResponse = new ResponseEntity<>(patientBundle("123"), HttpStatus.ACCEPTED);
    var immunizationBundleResponse =
        new ResponseEntity<>(immunizationBundle("123"), HttpStatus.ACCEPTED);
    var controller = patientController(patientBundleResponse, immunizationBundleResponse);
    var result = controller.issueVc("123", parametersCovid19(), "").getBody();
    assertNotNull(result);
    var vc = findVcFromParameters(result);
    assertThat(vc.context()).isEqualTo(List.of("https://www.w3.org/2018/credentials/v1"));
    assertThat(vc.type()).contains("VerifiableCredential", "https://smarthealth.cards#covid19");
    var fhirBundle = vc.credentialSubject().fhirBundle();
    assertThat(fhirBundle.entry()).hasSize(fhirBundle.total());
    assertThat(countEntriesByType(fhirBundle, "Patient")).isEqualTo(1);
    assertThat(countEntriesByType(fhirBundle, "Immunization")).isEqualTo(2);
  }

  @Test
  void issueVc_EmptyParameters() {
    var controller = patientController(null, null);
    // Empty List
    assertThrows(
        Exceptions.BadRequest.class, () -> controller.issueVc("123", parametersEmpty(), ""));
    // null List
    assertThrows(
        Exceptions.BadRequest.class,
        () -> controller.issueVc("123", parametersEmpty().parameter(null), ""));
  }

  @Test
  void issueVc_invalidCredentialType() {
    var controller = patientController(null, null);
    assertThrows(
        Exceptions.InvalidCredentialType.class,
        () -> controller.issueVc("123", parametersWithCredentialType("NOPE"), ""));
  }

  @Test
  void issueVc_unimplementedCredentialType() {
    var controller = patientController(null, null);
    assertThrows(
        Exceptions.NotImplemented.class,
        () ->
            controller.issueVc(
                "123", parametersWithCredentialType("https://smarthealth.cards#immunization"), ""));
  }
}
