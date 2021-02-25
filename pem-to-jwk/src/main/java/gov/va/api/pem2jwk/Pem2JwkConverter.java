package gov.va.api.pem2jwk;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.util.Base64URL;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

@Data
@Builder
public class Pem2JwkConverter {
  String pemContents;

  /** Convert PEM contents to JWK format. */
  @SneakyThrows
  public JwkOutput toJwk() {
    JWK jwk = JWK.parseFromPEMEncodedObjects(pemContents);
    Base64URL thumb = jwk.computeThumbprint();

    JWK out =
        new ECKey.Builder(jwk.toECKey())
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(Algorithm.parse("ES256"))
            .keyID(thumb.toString())
            .build();

    return JwkOutput.builder()
        .type(jwk.getKeyType())
        .use(jwk.getKeyUse())
        .thumb(thumb)
        .jwk(jwk)
        .jwkFilled(out)
        .build();
  }

  @Data
  @Builder
  public static class JwkOutput {
    private KeyType type;
    private KeyUse use;
    private Base64URL thumb;
    private JWK jwk;
    private JWK jwkFilled;
  }
}
