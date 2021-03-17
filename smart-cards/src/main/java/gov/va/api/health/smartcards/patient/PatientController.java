package gov.va.api.health.smartcards.patient;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.smartcards.Controllers.checkRequestState;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.Parameters;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.smartcards.Controllers;
import gov.va.api.health.smartcards.DataQueryFhirClient;
import gov.va.api.health.smartcards.Exceptions;
import gov.va.api.health.smartcards.JacksonMapperConfig;
import gov.va.api.health.smartcards.R4MixedBundler;
import gov.va.api.health.smartcards.vc.CredentialType;
import gov.va.api.health.smartcards.vc.VerifiableCredential;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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

  private final R4MixedBundler bundler;

  /** Extracts resources from Bundle entries and pushes them to an existing List. */
  private static <R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>>
      void consumeBundle(B bundle, List<MixedEntry> target, Function<E, MixedEntry> transform) {
    bundle.entry().stream().map(transform).forEachOrdered(target::add);
  }

  private static List<CredentialType> getCredentialTypes(Parameters parameters) {
    checkRequestState(parameters.parameter() != null, "parameters are required");
    return parameters.parameter().stream()
        .filter(p -> "credentialType".equals(p.name()))
        .map(Parameters.Parameter::valueUri)
        .map(CredentialType::fromUri)
        .collect(toList());
  }

  private static List<String> indexAndReplaceUrls(List<MixedEntry> entries) {
    List<String> urls =
        entries.stream()
            .map(AbstractEntry::fullUrl)
            .filter(s -> isNotBlank(s))
            .distinct()
            .collect(toCollection(ArrayList::new));
    // Index unique URLs and replace with 'resource:X' scheme
    for (MixedEntry entry : entries) {
      if (isBlank(entry.fullUrl())) {
        continue;
      }
      entry.fullUrl(RESOURCE_PREFIX + urls.indexOf(entry.fullUrl()));
      if (entry.resource() instanceof Immunization) {
        Immunization imm = (Immunization) entry.resource();
        String patientRef =
            Optional.of(imm).map(im -> im.patient()).map(p -> p.reference()).orElse(null);
        if (patientRef != null) {
          if (!urls.contains(patientRef)) {
            urls.add(patientRef);
          }
          imm.patient().reference(RESOURCE_PREFIX + urls.indexOf(patientRef));
        }
      }
    }
    return urls;
  }

  private static MixedEntry transform(Patient.Entry entry) {
    return PatientTransformer.builder().entry(entry).build().transform();
  }

  private static MixedEntry transform(Immunization.Entry entry) {
    return ImmunizationTransformer.builder().entry(entry).build().transform();
  }

  private static List<CredentialType> validateCredentialTypes(List<CredentialType> credentials) {
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

  private static VerifiableCredential vc(MixedBundle bundle, List<CredentialType> credentialTypes) {
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
    checkState(isNotBlank(id), "id is required");
    var credentialTypes = getCredentialTypes(parameters);
    validateCredentialTypes(credentialTypes);
    Patient.Bundle patients = fhirClient.patientBundle(id, authorization);
    Immunization.Bundle immunizations = fhirClient.immunizationBundle(id, authorization);
    lookupAndAttachLocations(immunizations, authorization);
    List<MixedEntry> resources = new ArrayList<>();
    consumeBundle(patients, resources, PatientController::transform);
    consumeBundle(immunizations, resources, PatientController::transform);
    List<String> urls = indexAndReplaceUrls(resources);
    var vc = vc(bundler.bundle(resources), credentialTypes);
    var parametersResponse = parameters(vc, urls);
    return ResponseEntity.ok(parametersResponse);
  }

  private void lookupAndAttachLocations(Immunization.Bundle immunizations, String authorization) {
    // keep track of locations we already looked up
    Map<String, Location> locations = new HashMap<>();
    for (Immunization.Entry entry : immunizations.entry()) {
      Immunization imm = entry.resource();
      String locId = Controllers.resourceId(imm.location());
      if (locId == null) {
        continue;
      }
      Location location = locations.get(locId);
      if (location == null) {
        location = fhirClient.location(locId, authorization);
        locations.put(locId, location);
      }
      imm.contained(
          Stream.concat(
                  Optional.ofNullable(imm.contained()).map(c -> c.stream()).orElse(Stream.empty()),
                  Stream.of(location))
              .collect(toList()));
    }
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
}
