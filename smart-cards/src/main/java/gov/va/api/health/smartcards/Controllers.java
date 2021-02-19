package gov.va.api.health.smartcards;

import static com.google.common.base.Preconditions.checkState;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Controllers {

  /** Wrapper for Preconditions.checkState which throws a BadRequest. */
  @SneakyThrows
  public static void checkRequestState(boolean condition, @NonNull String message) {
    try {
      checkState(condition, message);
    } catch (IllegalStateException e) {
      throw new Exceptions.BadRequest(e.getMessage(), e);
    }
  }

  /** Wrapper for Preconditions.checkState which throws a BadRequest. */
  public static void checkRequestState(
      boolean condition, String messageTemplate, Object... messageArgs) {
    try {
      checkState(condition, messageTemplate, messageArgs);
    } catch (IllegalStateException e) {
      throw new Exceptions.BadRequest(e.getMessage(), e);
    }
  }
}
