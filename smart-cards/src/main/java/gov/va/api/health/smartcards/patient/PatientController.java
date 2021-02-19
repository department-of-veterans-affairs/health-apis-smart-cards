package gov.va.api.health.smartcards.patient;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.smartcards.Controllers.checkRequestState;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Parameters;
import gov.va.api.health.r4.api.resources.Parameters.Parameter;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.smartcards.Exceptions;
import gov.va.api.health.smartcards.MockFhirClient;
import gov.va.api.health.smartcards.R4MixedBundler;
import gov.va.api.health.smartcards.vc.CredentialType;
import gov.va.api.health.smartcards.vc.VerifiableCredential;
import gov.va.api.health.smartcards.vc.VerifiableCredential.CredentialSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(
    value = "/r4/Patient",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class PatientController {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private static final List<CredentialType> UNIMPLEMENTED_CREDENTIAL_TYPES =
      List.of(
          CredentialType.immunization,
          CredentialType.presentation_context_online,
          CredentialType.presentation_context_in_person);

  private final MockFhirClient mockFhirClient;

  R4MixedBundler bundler;

  /** Extracts resources from Bundle entries and pushes them to an existing List. */
  private <R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>>
      void consumeBundle(B bundle, List<MixedEntry> target, Function<E, MixedEntry> transform) {
    bundle.entry().stream().map(transform).forEachOrdered(target::add);
  }

  private Patient.Bundle findPatientById(String id) {
    return mockFhirClient.patientBundle(id);
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
  ResponseEntity<Parameters> issueVc(
      @PathVariable("id") String id, @Valid @RequestBody Parameters parameters) {
    checkState(!StringUtils.isEmpty(id), "id is required");
    checkRequestState(parameters.parameter() != null, "parameters are required");
    validateCredentialType(parameters);
    Patient.Bundle patients = findPatientById(id);
    Patient patient = getPatientFromBundle(patients, id);
    Immunization.Bundle immunizations = mockFhirClient.immunizationBundle(patient);
    List<MixedEntry> resources = new ArrayList<>();
    consumeBundle(patients, resources, this::transform);
    consumeBundle(immunizations, resources, this::transform);
    MixedBundle bundle = toBundle(resources);
    var vc = vc(bundle);
    var parametersResponse = parameters(vc);
    return ResponseEntity.ok(parametersResponse);
  }

  @SneakyThrows
  private Parameters parameters(VerifiableCredential vc) {
    return Parameters.builder()
        .parameter(
            List.of(
                Parameters.Parameter.builder()
                    .name("verifiableCredential")
                    .valueString(MAPPER.writeValueAsString(vc))
                    .build()))
        .build();
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

  private void validateCredentialType(Parameters parameters) {
    var params =
        parameters.parameter().stream()
            .filter(p -> "credentialType".equals(p.name()))
            .map(Parameter::valueUri)
            .map(CredentialType::fromUri)
            .collect(toList());
    if (params.isEmpty()) {
      throw new Exceptions.BadRequest("credentialType parameter is required");
    }

    var requestedButUnimplemented =
        UNIMPLEMENTED_CREDENTIAL_TYPES.stream()
            .filter(u -> params.contains(u))
            .map(CredentialType::getUri)
            .collect(toList());

    if (!requestedButUnimplemented.isEmpty()) {
      throw new Exceptions.NotImplemented(
          String.format("Not yet implemented support for %s", requestedButUnimplemented));
    }
  }

  private VerifiableCredential vc(MixedBundle bundle) {
    return VerifiableCredential.builder()
        .context(List.of("https://www.w3.org/2018/credentials/v1"))
        .type(List.of("VerifiableCredential", "https://smarthealth.cards#covid19"))
        .credentialSubject(CredentialSubject.builder().fhirBundle(bundle).build())
        .build();
  }
}
