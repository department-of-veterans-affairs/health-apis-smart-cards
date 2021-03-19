package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class WellKnownJwksControllerTest {

  @Test
  public void test() {
    var jwksProperties = JwsHelpers.jwksProperties("123");
    var controller = new WellKnownJwksController(jwksProperties);
    assertThat(controller.read()).isEqualTo(jwksProperties.jwksPublicJson());
  }
}
