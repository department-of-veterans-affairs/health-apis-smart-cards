package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class Dstu2OpenApiControllerTest {
  @Test
  void openApi() {
    assertThat(new Dstu2OpenApiController().openApi()).isEqualTo("{}");
  }
}
