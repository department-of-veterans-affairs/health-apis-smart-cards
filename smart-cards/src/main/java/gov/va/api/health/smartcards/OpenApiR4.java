package gov.va.api.health.smartcards;

import gov.va.api.health.smartcards.patient.R4SmartCardsPatientApi;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Path;

@OpenAPIDefinition(
    info = @Info(title = "SMART Health Cards", version = "v1"),
    servers = {
      @Server(url = "https://sandbox-api.va.gov/services/fhir/v0/r4/", description = "Sandbox")
    },
    externalDocs =
        @ExternalDocumentation(
            description = "Smart Cards Implementation Guide",
            url = "https://smarthealth.cards"))
@Path("/")
public interface OpenApiR4 extends R4SmartCardsPatientApi {}
