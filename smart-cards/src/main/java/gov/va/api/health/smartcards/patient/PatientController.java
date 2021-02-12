package gov.va.api.health.smartcards.patient;

import static com.google.common.base.Preconditions.checkState;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.smartcards.Exceptions;
import gov.va.api.health.smartcards.MockFhirClient;
import gov.va.api.health.smartcards.R4MixedBundler;
import gov.va.api.health.smartcards.vc.VerifiableCredential;
import gov.va.api.health.smartcards.vc.VerifiableCredential.CredentialSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(
    value = "/r4/Patient",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class PatientController {
  private final MockFhirClient mockFhirClient;

  R4MixedBundler bundler;

  /** Extracts resources from Bundle entries and pushes them to an existing List. */
  private <R extends Resource, E extends AbstractEntry<R>> void consumeBundle(
      AbstractBundle<E> bundle, List<Resource> target, Function<R, R> transform) {
    bundle.entry().stream().map(AbstractEntry::resource).map(transform).forEachOrdered(target::add);
  }

  private Patient findPatientById(String id) {
    Optional<Patient> maybePatient = mockFhirClient.patient(id);
    return maybePatient.orElseThrow(() -> new Exceptions.NotFound(id));
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @PostMapping(value = "/{id}/$HealthWallet.issueVc")
  @SneakyThrows
  ResponseEntity<VerifiableCredential> issueVc(@PathVariable("id") String id) {
    checkState(!StringUtils.isEmpty(id), "id is required");
    Patient patient = findPatientById(id);
    Immunization.Bundle immunizations = mockFhirClient.immunizationBundle(patient);
    List<Resource> resources = new ArrayList<>();
    resources.add(transform(patient));
    consumeBundle(immunizations, resources, this::transform);
    MixedBundle bundle = toBundle(resources);
    var vc = vc(bundle);
    return ResponseEntity.ok(vc);
  }

  private MixedBundle toBundle(List<Resource> resources) {
    return bundler.bundle(resources);
  }

  private Patient transform(Patient patient) {
    return PatientTransformer.builder().patient(patient).build().transform();
  }

  private Immunization transform(Immunization immunization) {
    return ImmunizationTransformer.builder().immunization(immunization).build().transform();
  }

  private VerifiableCredential vc(MixedBundle bundle) {
    return VerifiableCredential.builder()
        .context(List.of("https://www.w3.org/2018/credentials/v1"))
        .type(List.of("VerifiableCredential", "https://smarthealth.cards#covid19"))
        .credentialSubject(CredentialSubject.builder().fhirBundle(bundle).build())
        .build();
  }
}
