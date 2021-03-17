package gov.va.api.health.smartcards;

import static java.util.stream.Collectors.toList;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JwsHelpers {
  public static String decompress(byte[] input) {
    return new String(Compressors.inflate(input), StandardCharsets.UTF_8);
  }

  @SneakyThrows
  public static ECKey genEcJwk(String kid) {
    return new ECKeyGenerator(Curve.P_256).keyID(kid).generate();
  }

  @SneakyThrows
  public static JwksProperties jwksProperties(String kid) {
    return jwksProperties(new String[] {kid});
  }

  @SneakyThrows
  public static JwksProperties jwksProperties(String... kids) {
    List<JWK> keys = Arrays.stream(kids).map(JwsHelpers::genEcJwk).collect(toList());
    JWKSet jwks = new JWKSet(keys);
    String jwkSet = jwks.toString(false);
    return new JwksProperties(jwkSet, kids[0]);
  }

  @SneakyThrows
  public static JWSObject parse(String jws) {
    return JWSObject.parse(jws);
  }

  @SneakyThrows
  public boolean verify(String jws, JWK publicJwk) {
    JWSVerifier verifier = new ECDSAVerifier(publicJwk.toECKey());
    JWSObject jwsObject = JWSObject.parse(jws);
    return jwsObject.verify(verifier);
  }
}
