package gov.va.api.health.smartcards.vc;

import gov.va.api.health.smartcards.Exceptions;
import java.util.Arrays;

public enum CredentialType {
  // Purposely NOT alphabetized!
  // These types become more granular the lower they are in this list.
  // We want to keep the enum's natural order (by order of declaration) when sorting!
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
