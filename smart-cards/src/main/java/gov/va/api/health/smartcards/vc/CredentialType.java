package gov.va.api.health.smartcards.vc;

import gov.va.api.health.smartcards.Exceptions;
import java.util.Arrays;

public enum CredentialType {
  COVID_19("https://smarthealth.cards#covid19"),
  IMMUNIZATION("https://smarthealth.cards#immunization"),
  PRESENTATION_CONTEXT_ONLINE("https://smarthealth.cards#presentation-context-online"),
  PRESENTATION_CONTEXT_IN_PERSON("https://smarthealth.cards#presentation-context-in-person");

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
