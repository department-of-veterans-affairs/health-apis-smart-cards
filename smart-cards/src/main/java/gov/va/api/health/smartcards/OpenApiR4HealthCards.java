package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Parameters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

public interface OpenApiR4HealthCards {
  @POST
  @Operation(summary = "Issues Verifiable Credential")
  @Path("Patient/{id}/$health-cards-issue")
  @ApiResponse(
      responseCode = "200",
      description = "Verifiable Credential Received",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = Parameters.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  Parameters healthCardsIssue(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description = "The ID of the patient to issue credentials for.")
          String id,
      @RequestBody(required = true, description = "Parameters used for credential type validation")
          Parameters parameters);
}
