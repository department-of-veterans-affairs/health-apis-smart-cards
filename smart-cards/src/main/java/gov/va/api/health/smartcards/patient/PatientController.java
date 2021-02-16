package gov.va.api.health.smartcards.patient;

import static com.google.common.base.Preconditions.checkState;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.bundle.MixedEntry;
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
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.NonNull;
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
  private <R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>>
      void consumeBundle(B bundle, List<MixedEntry> target, Function<E, MixedEntry> transform) {
    bundle.entry().stream().map(transform).forEachOrdered(target::add);
  }

  private Patient.Bundle findPatientById(String id) {
    Patient.Bundle patientBundle = mockFhirClient.patientBundle(id);
    if (patientBundle.total() == 0) {
      throw new Exceptions.NotFound(id);
    }
    return patientBundle;
  }

  private Patient getPatientFromBundle(Patient.Bundle bundle, @NonNull String id) {
    var entry = bundle.entry().stream().filter(t -> id.equals(t.resource().id())).findFirst();
    if (entry.isPresent()) {
      return entry.get().resource();
    }
    throw new Exceptions.NotFound(id);
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @PostMapping(value = "/{id}/$HealthWallet.issueVc")
  @SneakyThrows
  ResponseEntity<VerifiableCredential> issueVc(@PathVariable("id") String id) {
    checkState(!StringUtils.isEmpty(id), "id is required");
    Patient.Bundle patients = findPatientById(id);
    Patient patient = getPatientFromBundle(patients, id);

    Immunization.Bundle immunizations = mockFhirClient.immunizationBundle(patient);
    List<MixedEntry> resources = new ArrayList<>();

    consumeBundle(patients, resources, this::transform);
    consumeBundle(immunizations, resources, this::transform);
    MixedBundle bundle = toBundle(resources);
    var vc = vc(bundle);
    return ResponseEntity.ok(vc);
  }

  private MixedBundle toBundle(List<MixedEntry> resources) {
    return bundler.bundle(resources);
  }

  private MixedEntry transform(Patient.Entry entry) {
    return PatientTransformer.builder().entry(entry).build().transform();
  }

  private MixedEntry transform(Immunization.Entry entry) {
    return ImmunizationTransformer.builder().entry(entry).build().transform();
  }

  private VerifiableCredential vc(MixedBundle bundle) {
    return VerifiableCredential.builder()
        .context(List.of("https://www.w3.org/2018/credentials/v1"))
        .type(List.of("VerifiableCredential", "https://smarthealth.cards#covid19"))
        .credentialSubject(CredentialSubject.builder().fhirBundle(bundle).build())
        .build();
  }
}
