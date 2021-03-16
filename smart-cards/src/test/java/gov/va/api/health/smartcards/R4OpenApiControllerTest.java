package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class R4OpenApiControllerTest {
  @Test
  void openApi() {
    assertThat(new R4OpenApiController().openApi()).isEqualTo("{}");
  }
}
