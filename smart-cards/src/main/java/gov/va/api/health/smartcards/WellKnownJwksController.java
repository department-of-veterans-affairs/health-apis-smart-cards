package gov.va.api.health.smartcards;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    path = "/.well-known/jwks.json",
    produces = {"application/json", "application/fhir+json", "application/json+fhir"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class WellKnownJwksController {

  private final JwksProperties jwksProperties;

  @GetMapping
  public String read() {
    return jwksProperties.jwksPublicJson();
  }
}
