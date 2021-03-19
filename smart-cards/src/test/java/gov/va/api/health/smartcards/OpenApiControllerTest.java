package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class OpenApiControllerTest {
  @Test
  void dstu2() {
    assertThat(new OpenApiController().dstu2()).isEqualTo("{}");
  }

  @Test
  void r4() {
    assertThat(new OpenApiController().r4()).isEqualTo("{}");
  }
}
