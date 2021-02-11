package gov.va.api.health.smartcards;

import static java.util.Collections.singletonList;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.Annotation;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.HumanName.NameUse;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Identifier.IdentifierUse;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Immunization.Status;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Patient.Gender;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class MockFhirClient implements FhirClient {
  private static final String VACCINE_SYSTEM = "http://hl7.org/fhir/sid/cvx";

  private final LinkProperties linkProperties;

  @Override
  public Immunization.Bundle immunizationBundle(Patient patient) {
    var immunizations = immunizations(patient);
    return Immunization.Bundle.builder()
        .type(AbstractBundle.BundleType.searchset)
        .link(
            singletonList(
                BundleLink.builder()
                    .relation(BundleLink.LinkRelation.self)
                    .url(
                        String.format(
                            "%s?patient=%s",
                            linkProperties.r4ResourceUrl("Immunization"), patient.id()))
                    .build()))
        .total(immunizations.size())
        .entry(
            immunizations.stream()
                .map(
                    t -> {
                      return Immunization.Entry.builder()
                          .resource(t)
                          .fullUrl(linkProperties.r4ReadUrl(t))
                          .search(
                              AbstractEntry.Search.builder()
                                  .mode(AbstractEntry.SearchMode.match)
                                  .build())
                          .build();
                    })
                .collect(Collectors.toList()))
        .build();
  }

  private List<Immunization> immunizations(Patient patient) {
    return List.of(
        Immunization.builder()
            .resourceType("Immunization")
            .id(String.format("imm-1-%s", patient.id()))
            .status(Status.completed)
            .vaccineCode(
                CodeableConcept.builder()
                    .coding(List.of(Coding.builder().system(VACCINE_SYSTEM).code("207").build()))
                    .text("COVID-19, mRNA, LNP-S, PF, 100 mcg/ 0.5 mL dose")
                    .build())
            .patient(
                Reference.builder()
                    .reference(linkProperties.r4ReadUrl(patient))
                    .display(patient.name().stream().findFirst().get().text())
                    .build())
            .occurrenceDateTime("2020-12-18T12:24:55Z")
            .primarySource(true)
            .location(
                Reference.builder()
                    .reference(
                        String.format(
                            "%s/loc-%s", linkProperties.r4ResourceUrl("Location"), patient.id()))
                    .display("Location for " + patient.id())
                    .build())
            .note(
                singletonList(
                    Annotation.builder()
                        .text(
                            "Dose #1 of 2 of COVID-19, mRNA, LNP-S, PF, 100 mcg/ 0.5 mL dose "
                                + "vaccine administered.")
                        .build()))
            .build(),
        Immunization.builder()
            .resourceType("Immunization")
            .id(String.format("imm-2-%s", patient.id()))
            .status(Status.completed)
            .vaccineCode(
                CodeableConcept.builder()
                    .coding(List.of(Coding.builder().system(VACCINE_SYSTEM).code("207").build()))
                    .text("COVID-19, mRNA, LNP-S, PF, 100 mcg/ 0.5 mL dose")
                    .build())
            .patient(
                Reference.builder()
                    .reference(linkProperties.r4ReadUrl(patient))
                    .display(patient.name().stream().findFirst().get().text())
                    .build())
            .occurrenceDateTime("2021-01-14T09:30:21Z")
            .primarySource(true)
            .location(
                Reference.builder()
                    .reference(
                        String.format(
                            "%s/loc-%s", linkProperties.r4ResourceUrl("Location"), patient.id()))
                    .display("Location for " + patient.id())
                    .build())
            .note(
                singletonList(
                    Annotation.builder()
                        .text(
                            "Dose #2 of 2 of COVID-19, mRNA, LNP-S, PF, 100 mcg/ 0.5 mL dose "
                                + "vaccine administered.")
                        .build()))
            .build());
  }

  @Override
  public Optional<Patient> patient(String id) {
    if ("404".equals(id)) {
      return Optional.empty();
    }
    String firstName = "Joe" + id;
    String lastName = "Doe" + id;
    return Optional.of(
        Patient.builder()
            .resourceType("Patient")
            .id(id)
            .identifier(List.of(Identifier.builder().id(id).use(IdentifierUse.temp).build()))
            .active(true)
            .name(
                singletonList(
                    HumanName.builder()
                        .use(NameUse.anonymous)
                        .text(String.format("%s %s", firstName, lastName))
                        .family(lastName)
                        .given(singletonList(firstName))
                        .build()))
            .gender(Gender.unknown)
            .birthDate("1955-01-01")
            .deceasedBoolean(false)
            .build());
  }
}