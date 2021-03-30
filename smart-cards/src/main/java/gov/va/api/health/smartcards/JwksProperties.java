package gov.va.api.health.smartcards;

import static com.google.common.base.Preconditions.checkState;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@ToString
@Component
public final class JwksProperties {
  private final String currentKeyId;

  private final JWKSet jwksPrivate;

  private final JWKSet jwksPublic;

  private final String jwksPublicJson;

  @Builder
  @SneakyThrows
  JwksProperties(
      @Value("${jwk-set.private-json}") String jwksPrivateJson,
      @Value("${jwk-set.current-key-id}") String currentKeyId) {
    checkState(!"unset".equals(jwksPrivateJson), "jwk-set.private-json is unset");
    checkState(!"unset".equals(currentKeyId), "jwk-set.current-key-id is unset");
    this.currentKeyId = currentKeyId;
    jwksPrivate = JWKSet.parse(jwksPrivateJson);
    jwksPublic = jwksPrivate.toPublicJWKSet();
    jwksPublicJson = jwksPublic.toString();
  }

  public JWK currentPrivateJwk() {
    return jwksPrivate.getKeyByKeyId(currentKeyId);
  }

  public JWK currentPublicJwk() {
    return jwksPublic.getKeyByKeyId(currentKeyId);
  }
}
