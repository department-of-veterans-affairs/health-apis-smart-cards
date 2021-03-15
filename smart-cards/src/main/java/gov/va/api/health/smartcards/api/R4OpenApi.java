package gov.va.api.health.smartcards.api;

import gov.va.api.health.smartcards.patient.SmartCardsPatientApi;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Path;

@OpenAPIDefinition(
    info = @Info(title = "US Core R4", version = "v1"),
    servers = {
      @Server(url = "https://sandbox-api.va.gov/services/pgd/v0/r4/", description = "Sandbox")
    },
    externalDocs =
        @ExternalDocumentation(
            description = "US Core Implementation Guide",
            url = "https://build.fhir.org/ig/HL7/US-Core-R4/index.html"))
@Path("/")
public interface R4OpenApi extends SmartCardsPatientApi {}
