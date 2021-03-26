package gov.va.api.health.smartcards.patient;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.smartcards.Controllers.checkRequestState;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.Parameters;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.smartcards.Controllers;
import gov.va.api.health.smartcards.DataQueryFhirClient;
import gov.va.api.health.smartcards.Exceptions;
import gov.va.api.health.smartcards.JacksonMapperConfig;
import gov.va.api.health.smartcards.PayloadSigner;
import gov.va.api.health.smartcards.vc.CredentialType;
import gov.va.api.health.smartcards.vc.VerifiableCredential;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.validation.Valid;
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

  private static final String RESOURCE_PREFIX = "resource:";

  private static final List<CredentialType> UNIMPLEMENTED_CREDENTIAL_TYPES =
      List.of(CredentialType.LABORATORY);

  private final DataQueryFhirClient fhirClient;

  private final PayloadSigner payloadSigner;

  private static MixedBundle bundle(List<MixedEntry> entries) {
    return MixedBundle.builder()
        .resourceType("Bundle")
        .type(AbstractBundle.BundleType.collection)
        .entry(entries)
        .build();
  }

  private static <R extends Resource, E extends AbstractEntry<R>, B extends AbstractBundle<E>>
      void consumeBundle(B bundle, List<MixedEntry> target, Function<E, MixedEntry> minimizer) {
    bundle.entry().stream().map(minimizer).forEachOrdered(target::add);
  }

  static Set<CredentialType> credentialTypes(Parameters parameters) {
    checkRequestState(parameters.parameter() != null, "parameters are required");
    Set<CredentialType> types =
        parameters.parameter().stream()
            .filter(p -> "credentialType".equals(p.name()))
            .map(Parameters.Parameter::valueUri)
            .map(CredentialType::fromUri)
            .collect(toSet());
    validateCredentialTypes(types);
    // Expand credential types.
    types.add(CredentialType.HEALTH_CARD);
    // Just covid19 assumes immunizations by default
    if (types.contains(CredentialType.COVID_19)) {
      types.add(CredentialType.IMMUNIZATION);
    }
    return types;
  }

  private static List<String> indexAndReplaceUrls(List<MixedEntry> entries) {
    List<String> urls =
        entries.stream()
            .map(AbstractEntry::fullUrl)
            .filter(StringUtils::isNotBlank)
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
            Optional.of(imm).map(Immunization::patient).map(Reference::reference).orElse(null);
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

  private static MixedEntry minimize(Patient.Entry entry) {
    return PatientMinimizer.builder().entry(entry).build().minimize();
  }

  private static MixedEntry minimize(Immunization.Entry entry) {
    return ImmunizationMinimizer.builder().entry(entry).build().minimize();
  }

  private static List<MixedEntry> minimize(
      Patient.Bundle patient, Immunization.Bundle immunizations) {
    List<MixedEntry> resources = new ArrayList<>();
    consumeBundle(patient, resources, PatientController::minimize);
    consumeBundle(immunizations, resources, PatientController::minimize);
    return resources;
  }

  private static List<Parameters.Parameter> parameterResourceLinks(List<String> urls) {
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
  private static Parameters parameters(String vc, List<String> urls) {
    return Parameters.builder()
        .parameter(
            Stream.concat(
                    Stream.of(
                        Parameters.Parameter.builder()
                            .name("verifiableCredential")
                            .valueString(vc)
                            .build()),
                    parameterResourceLinks(urls).stream())
                .collect(toList()))
        .build();
  }

  private static boolean parseBooleanOrTrue(String value) {
    // not using parseBoolean because we need to default to true
    return !"false".equalsIgnoreCase(StringUtils.trimToEmpty(value));
  }

  private static void validateCredentialTypes(Set<CredentialType> credentials) {
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
    // Reject a request with ONLY #health-card
    if (credentials.equals(Set.of(CredentialType.HEALTH_CARD))) {
      throw new Exceptions.BadRequest("Specify a more granular credential type");
    }
    // Reject a request with only #immunization
    if (credentials.contains(CredentialType.IMMUNIZATION)
        && !credentials.contains(CredentialType.COVID_19)) {
      throw new Exceptions.NotImplemented("Only support covid19 credential type");
    }
  }

  private static VerifiableCredential vc(MixedBundle bundle, Set<CredentialType> credentialTypes) {
    return VerifiableCredential.builder()
        .context(List.of("https://www.w3.org/2018/credentials/v1"))
        .type(
            Stream.concat(
                    Stream.of("VerifiableCredential"),
                    credentialTypes.stream().sorted().map(CredentialType::getUri))
                .collect(toList()))
        .credentialSubject(
            VerifiableCredential.CredentialSubject.builder()
                .fhirVersion("4.0.1")
                .fhirBundle(bundle)
                .build())
        .build();
  }

  private Patient.Bundle findPatientByIcn(String icn, String authorization) {
    var patients = fhirClient.patientBundle(icn, authorization);
    if (patients.total() == 0) {
      throw new Exceptions.NotFound(icn);
    }
    return patients;
  }

  @PostMapping(value = "/{id}/$health-cards-issue")
  ResponseEntity<Parameters> healthCardsIssue(
      @PathVariable("id") String id,
      @Valid @RequestBody Parameters parameters,
      @RequestHeader(name = "Authorization", required = false) String authorization,
      @RequestHeader(name = "x-vc-jws", required = false) String vcJws,
      @RequestHeader(name = "x-vc-compress", required = false) String vcCompress) {
    checkState(isNotBlank(id), "id is required");
    var credentialTypes = credentialTypes(parameters);
    Patient.Bundle patients = findPatientByIcn(id, authorization);
    Immunization.Bundle immunizations = fhirClient.immunizationBundle(id, authorization);
    if (immunizations.total() == 0) {
      return ResponseEntity.ok(Parameters.builder().build());
    }
    lookupAndAttachLocations(immunizations, authorization);
    List<MixedEntry> resources = minimize(patients, immunizations);
    List<String> urls = indexAndReplaceUrls(resources);
    var vc = vc(bundle(resources), credentialTypes);
    var signedVc = signVc(vc, parseBooleanOrTrue(vcJws), parseBooleanOrTrue(vcCompress));
    var parametersResponse = parameters(signedVc, urls);
    return ResponseEntity.ok(parametersResponse);
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
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
                  Optional.ofNullable(imm.contained()).stream().flatMap(Collection::stream),
                  Stream.of(location))
              .collect(toList()));
    }
  }

  @SneakyThrows
  private String signVc(VerifiableCredential vc, boolean shouldSign, boolean shouldCompress) {
    if (!shouldSign) {
      return MAPPER.writeValueAsString(vc);
    }
    return payloadSigner.sign(vc, shouldCompress);
  }
}
