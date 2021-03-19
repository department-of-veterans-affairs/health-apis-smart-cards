package gov.va.api.health.smartcards.patient;

import gov.va.api.health.r4.api.resources.Parameters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.springframework.http.ResponseEntity;

public interface R4SmartCardsPatientApi {
  @POST
  @Operation(summary = "Issues Verifiable Credential")
  @Path("Patient/{id}/$health-cards-issue")
  @ApiResponse(responseCode = "200", description = "Verifiable Credential Received")
  ResponseEntity<Parameters> healthCardsIssue(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description = "The ID of the patient to issue credentials for.")
          String id,
      @RequestBody(required = true, description = "Parameters used for credential type validation")
          Parameters parameters);
}
