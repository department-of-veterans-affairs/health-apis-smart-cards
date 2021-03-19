package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ControllersTest {
  @Test
  void badRequest() {
    Throwable ex =
        assertThrows(
            Exceptions.BadRequest.class, () -> Controllers.checkRequestState(false, "message"));
    assertThat(ex.getMessage()).isEqualTo("message");

    ex =
        assertThrows(
            Exceptions.BadRequest.class,
            () -> Controllers.checkRequestState(false, "%s foo %s", "hello", "goodbye"));
    assertThat(ex.getMessage()).isEqualTo("hello foo goodbye");
  }

  @Test
  void resourceId() {
    assertThat(Controllers.resourceId((String) null)).isNull();
    assertThat(Controllers.resourceId(" ")).isNull();
    assertThat(Controllers.resourceId(" x ")).isNull();
    assertThat(Controllers.resourceId(" / ")).isNull();
    assertThat(Controllers.resourceId(" / / / ")).isNull();
    assertThat(Controllers.resourceId(" x / ")).isNull();
    assertThat(Controllers.resourceId(" x / y")).isEqualTo("y");
  }
}
