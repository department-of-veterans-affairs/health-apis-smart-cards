package gov.va.api.health.smartcards.vc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PayloadClaimsWrapper {
  @JsonProperty("iss")
  String issuer;

  @JsonProperty("iat")
  long issuedAt;

  @JsonProperty("vc")
  VerifiableCredential verifiableCredential;
}
