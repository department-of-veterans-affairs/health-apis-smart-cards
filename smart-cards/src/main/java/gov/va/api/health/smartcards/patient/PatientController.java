package gov.va.api.health.smartcards.patient;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.smartcards.Controllers.checkRequestState;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Immunization.Status;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.Parameters;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.smartcards.Controllers;
import gov.va.api.health.smartcards.DataQueryFhirClient;
import gov.va.api.health.smartcards.Exceptions;
import gov.va.api.health.smartcards.JacksonMapperConfig;
import gov.va.api.health.smartcards.MockFhirClient;
import gov.va.api.health.smartcards.R4MixedBundler;
import gov.va.api.health.smartcards.vc.CredentialType;
import gov.va.api.health.smartcards.vc.VerifiableCredential;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(
    value = "/r4/Patient",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class PatientController {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  private static final List<CredentialType> UNIMPLEMENTED_CREDENTIAL_TYPES =
      List.of(
          CredentialType.IMMUNIZATION,
          CredentialType.PRESENTATION_CONTEXT_ONLINE,
          CredentialType.PRESENTATION_CONTEXT_IN_PERSON);

  private static final String RESOURCE_PREFIX = "resource:";

  private final DataQueryFhirClient fhirClient;

  private final MockFhirClient mockFhirClient;

  R4MixedBundler bundler;

  /** Extracts resources from Bundle entries and pushes them to an existing List. */
  private <R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>>
      void consumeBundle(
          B bundle,
          List<MixedEntry> target,
          Predicate<E> filter,
          Function<E, MixedEntry> transform) {
    bundle.entry().stream().filter(filter).map(transform).forEachOrdered(target::add);
  }

  private <R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>>
      void consumeBundle(
          List<B> bundles,
          List<MixedEntry> target,
          Predicate<E> filter,
          Function<E, MixedEntry> transform) {
    for (B bundle : bundles) {
      consumeBundle(bundle, target, filter, transform);
    }
  }

  private boolean filter(Immunization.Entry entry) {
    return entry.resource().status() == Status.completed;
  }

  private List<CredentialType> getCredentialTypes(Parameters parameters) {
    checkRequestState(parameters.parameter() != null, "parameters are required");
    return parameters.parameter().stream()
        .filter(p -> "credentialType".equals(p.name()))
        .map(Parameters.Parameter::valueUri)
        .map(CredentialType::fromUri)
        .collect(toList());
  }

  private Patient getPatientFromBundle(Patient.Bundle bundle, @NonNull String id) {
    var entry = bundle.entry().stream().filter(t -> id.equals(t.resource().id())).findFirst();
    if (entry.isPresent()) {
      return entry.get().resource();
    }
    throw new Exceptions.NotFound(id);
  }

  private List<String> indexAndReplaceUrls(List<MixedEntry> resources) {
    List<String> urls = new ArrayList<>();
    resources.stream()
        .map(AbstractEntry::fullUrl)
        .filter(u -> !urls.contains(u))
        .forEachOrdered(urls::add);
    for (MixedEntry entry : resources) {
      entry.fullUrl(RESOURCE_PREFIX + urls.indexOf(entry.fullUrl()));
      if (entry.resource() instanceof Immunization) {
        Immunization imm = (Immunization) entry.resource();
        List<String> references = List.of(imm.patient().reference(), imm.location().reference());
        references.stream().filter(r -> !urls.contains(r)).forEachOrdered(urls::add);
        imm.location().reference(RESOURCE_PREFIX + urls.indexOf(imm.location().reference()));
        imm.patient().reference(RESOURCE_PREFIX + urls.indexOf(imm.patient().reference()));
      }
    }
    return urls;
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @SneakyThrows
  @PostMapping(value = "/{id}/$HealthWallet.issueVc")
  ResponseEntity<Parameters> issueVc(
      @PathVariable("id") String id,
      @Valid @RequestBody Parameters parameters,
      @RequestHeader(name = "Authorization", required = false) String authorization) {
    checkState(!StringUtils.isEmpty(id), "id is required");
    var credentialTypes = getCredentialTypes(parameters);
    validateCredentialTypes(credentialTypes);
    Patient.Bundle patients = fhirClient.patientBundle(id, authorization);
    Patient patient = getPatientFromBundle(patients, id);
    Immunization.Bundle immunizations = mockFhirClient.immunizationBundle(patient);
    List<Location.Bundle> locations = lookupLocations(immunizations);
    List<MixedEntry> resources = new ArrayList<>();
    consumeBundle(patients, resources, x -> true, this::transform);
    consumeBundle(immunizations, resources, this::filter, this::transform);
    consumeBundle(locations, resources, x -> true, this::transform);
    // Index unique URLs and replace with 'resource:X' scheme
    List<String> urls = indexAndReplaceUrls(resources);
    MixedBundle bundle = toBundle(resources);
    var vc = vc(bundle, credentialTypes);
    var parametersResponse = parameters(vc, urls);
    return ResponseEntity.ok(parametersResponse);
  }

  private List<Location.Bundle> lookupLocations(Immunization.Bundle immunizations) {
    // keep track of locations we already looked up
    List<String> uniqueLocations = new ArrayList<>();
    return immunizations.entry().stream()
        .map(i -> i.resource().location().reference())
        .map(Controllers::resourceId)
        .filter(l -> !uniqueLocations.contains(l))
        .peek(uniqueLocations::add)
        .map(mockFhirClient::locationBundle)
        .collect(toList());
  }

  private List<Parameters.Parameter> parameterResourceLinks(List<String> urls) {
    return urls.stream()
        .map(
            url ->
                Parameters.Parameter.builder()
                    .name("resourceLink")
                    .part(
                        List.of(
                            Parameters.Parameter.builder()
                                .name("bundledResource")
                                .valueUri("resource:" + urls.indexOf(url))
                                .build(),
                            Parameters.Parameter.builder()
                                .name("hostedResource")
                                .valueUri(url)
                                .build()))
                    .build())
        .collect(toList());
  }

  @SneakyThrows
  private Parameters parameters(VerifiableCredential vc, List<String> urls) {
    return Parameters.builder()
        .parameter(
            Stream.concat(
                    List.of(
                        Parameters.Parameter.builder()
                            .name("verifiableCredential")
                            .valueString(MAPPER.writeValueAsString(vc))
                            .build())
                        .stream(),
                    parameterResourceLinks(urls).stream())
                .collect(toList()))
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

  private MixedEntry transform(Location.Entry entry) {
    return LocationTransformer.builder().entry(entry).build().transform();
  }

  private List<CredentialType> validateCredentialTypes(List<CredentialType> credentials) {
    if (credentials.isEmpty()) {
      throw new Exceptions.BadRequest("credentialType parameter is required");
    }
    var requestedButUnimplemented =
        UNIMPLEMENTED_CREDENTIAL_TYPES.stream()
            .filter(credentials::contains)
            .map(CredentialType::getUri)
            .collect(toList());
    if (!requestedButUnimplemented.isEmpty()) {
      throw new Exceptions.NotImplemented(
          String.format("Not yet implemented support for %s", requestedButUnimplemented));
    }
    return credentials;
  }

  private VerifiableCredential vc(MixedBundle bundle, List<CredentialType> credentialTypes) {
    return VerifiableCredential.builder()
        .context(List.of("https://www.w3.org/2018/credentials/v1"))
        .type(
            Stream.concat(
                    Stream.of("VerifiableCredential"),
                    credentialTypes.stream().map(CredentialType::getUri))
                .collect(toList()))
        .credentialSubject(
            VerifiableCredential.CredentialSubject.builder().fhirBundle(bundle).build())
        .build();
  }
}
