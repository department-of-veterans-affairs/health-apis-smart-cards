package gov.va.api.health.smartcards.vc;

import gov.va.api.health.smartcards.Exceptions;
import java.util.Arrays;

public enum CredentialType {
  covid19("https://smarthealth.cards#covid19"),
  immunization("https://smarthealth.cards#immunization"),
  presentation_context_online("https://smarthealth.cards#presentation-context-online"),
  getPresentation_context_in_person("https://smarthealth.cards#presentation-context-in-person");

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
}
