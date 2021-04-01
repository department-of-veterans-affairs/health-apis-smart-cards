package gov.va.api.health.smartcards;

import java.util.Arrays;

public enum CredentialType {
  // ordered by granularity for sorting
  HEALTH_CARD("https://smarthealth.cards#health-card"),
  IMMUNIZATION("https://smarthealth.cards#immunization"),
  LABORATORY("https://smarthealth.cards#laboratory"),
  COVID_19("https://smarthealth.cards#covid19");

  private final String uri;

  CredentialType(String uri) {
    this.uri = uri;
  }

  /** Find CredentialType for uri value. */
  public static CredentialType fromUri(final String uri) {
    return Arrays.stream(CredentialType.values())
        .filter(c -> c.uri.equals(uri))
        .findFirst()
        .orElseThrow(() -> new Exceptions.InvalidCredentialType(uri));
  }

  public String getUri() {
    return uri;
  }
}
