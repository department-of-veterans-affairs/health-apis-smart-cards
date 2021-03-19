package gov.va.api.health.smartcards;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.Address;
import gov.va.api.health.r4.api.datatypes.Annotation;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.datatypes.ContactPoint.ContactPointSystem;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Location;
import gov.va.api.health.r4.api.resources.Location.Mode;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MockResourceSamples {
  LinkProperties linkProperties;

  public Immunization.Bundle immunizationBundle(String icn) {
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

  public Location location(String id) {
    return Location.builder()
        .id(id)
        .status(Location.Status.active)
        .name("Location with id " + id)
        .description("Description for id " + id)
        .mode(Mode.instance)
        .type(
            List.of(
                CodeableConcept.builder()
                    .coding(List.of(Coding.builder().display("SOME CODING").build()))
                    .text("SOME TEXT")
                    .build()))
        .telecom(
            List.of(
                ContactPoint.builder()
                    .system(ContactPointSystem.phone)
                    .value("123-456-7890 x0001")
                    .build()))
        .address(
            Address.builder()
                .text("1901 VETERANS MEMORIAL DRIVE TEMPLE TEXAS 76504")
                .line(List.of("1901 VETERANS MEMORIAL DRIVE"))
                .city("TEMPLE")
                .state("TEXAS")
                .postalCode("76504")
                .build())
        .physicalType(
            CodeableConcept.builder()
                .coding(List.of(Coding.builder().display("PHYS TYPE CODING").build()))
                .text("PHYS TYPE TEXT")
                .build())
        .managingOrganization(
            Reference.builder()
                .reference(linkProperties.dataQueryR4ResourceUrl("Organization") + "/org-" + id)
                .display("MNG ORG VA MEDICAL")
                .build())
        .build();
  }

  public Patient.Bundle patientBundle(String id) {
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
}
