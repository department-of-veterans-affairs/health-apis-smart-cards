package gov.va.api.health.smartcards;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class VerifiableCredential {
  @JsonProperty("@context")
  private List<String> context;

  private List<String> type;

  private CredentialSubject credentialSubject;

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class CredentialSubject {
    private String fhirVersion;

    private MixedBundle fhirBundle;
  }
}
