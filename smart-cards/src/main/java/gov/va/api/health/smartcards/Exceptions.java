package gov.va.api.health.smartcards;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Exceptions {

  public static final class BadRequest extends RuntimeException {
    public BadRequest(String message) {
      super(message);
    }

    public BadRequest(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static final class InvalidCredentialType extends RuntimeException {
    public InvalidCredentialType(String value) {
      super(String.format("Invalid credentialType %s", value));
    }
  }

  public static final class InvalidPayload extends RuntimeException {
    public InvalidPayload(String id, Throwable cause) {
      super(String.format("Resource %s has invalid payload", id), cause);
    }
  }

  public static final class NotFound extends RuntimeException {
    public NotFound(String message) {
      super(message);
    }
  }
}
