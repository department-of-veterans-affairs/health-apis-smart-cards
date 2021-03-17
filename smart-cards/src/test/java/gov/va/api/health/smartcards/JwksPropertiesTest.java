package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class JwksPropertiesTest {

  private static final ECKey KEY_CURRENT = genEcJwk("123");
  private static final ECKey KEY_OTHER = genEcJwk("456");

  @SneakyThrows
  private static JwksProperties _jwksProperties() {
    JWKSet jwks = new JWKSet(List.of(KEY_CURRENT, KEY_OTHER));
    String jwkSet = jwks.toString(false);
    return new JwksProperties(jwkSet, "123");
  }

  @SneakyThrows
  private static ECKey genEcJwk(String kid) {
    return new ECKeyGenerator(Curve.P_256).keyID(kid).generate();
  }

  @Test
  @SneakyThrows
  void test() {
    var jwksProperties = _jwksProperties();

    assertThat(jwksProperties.currentKeyId()).isEqualTo("123");
    assertThat(jwksProperties.currentPrivateJwk()).isEqualTo(KEY_CURRENT);
    assertThat(jwksProperties.currentPrivateJwk().isPrivate()).isTrue();

    assertThat(jwksProperties.currentPublicJwk()).isEqualTo(KEY_CURRENT.toPublicJWK());
    assertThat(jwksProperties.currentPublicJwk().isPrivate()).isFalse();

    // Public JWKS has no private information
    for (JWK jwk : jwksProperties.jwksPublic().getKeys()) {
      assertThat(jwk.isPrivate()).isFalse();
    }
  }

  //  @Test
  //  void testCleanupJson() {
  //    JWKSet jwks = new JWKSet(List.of(KEY_CURRENT, KEY_OTHER));
  //    String json = jwks.toString(false);
  //    assertDoesNotThrow(() -> new JwksProperties(String.format("\"%s\"", json), "123"));
  //    assertDoesNotThrow(() -> new JwksProperties(String.format("'%s'", json), "123"));
  //  }
}
