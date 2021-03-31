package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ControllersTest {
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
