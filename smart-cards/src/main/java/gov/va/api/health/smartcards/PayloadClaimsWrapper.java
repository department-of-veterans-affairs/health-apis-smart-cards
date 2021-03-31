package gov.va.api.health.smartcards;

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
public final class PayloadClaimsWrapper {
  @JsonProperty("iss")
  private String issuer;

  @JsonProperty("iat")
  private long issuedAt;

  @JsonProperty("vc")
  private VerifiableCredential verifiableCredential;
}
