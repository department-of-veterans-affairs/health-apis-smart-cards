package gov.va.api.health.smartcards;

import static com.google.common.base.Preconditions.checkState;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class JwksProperties {
  final String currentKeyId;

  final JWKSet jwksPrivate;

  final JWKSet jwksPublic;

  final String jwksPublicJson;

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
