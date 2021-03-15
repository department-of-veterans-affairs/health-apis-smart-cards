package gov.va.api.health.smartcards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.JWK;
import gov.va.api.health.smartcards.vc.PayloadClaimsWrapper;
import gov.va.api.health.smartcards.vc.VerifiableCredential;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import lombok.Builder;
import lombok.SneakyThrows;

@Builder
public class PayloadSigner {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  private final JwksProperties jwksProperties;
  private final LinkProperties linkProperties;

  private byte[] deflate(String payload) {
    return Compressors.deflate(payload.getBytes(StandardCharsets.UTF_8));
  }

  private JWSHeader jwsHeader(JWK jwk, boolean deflate) {
    JWSHeader.Builder headerBuilder =
        new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(jwk.getKeyID());
    if (deflate) {
      headerBuilder.customParam("zip", "DEF");
    }
    return headerBuilder.build();
  }

  /** Sign VC payload. Generates a JWS. */
  @SneakyThrows
  public String sign(VerifiableCredential vc, boolean deflate) {
    PayloadClaimsWrapper claims =
        PayloadClaimsWrapper.builder()
            .issuer(linkProperties.r4Url())
            .issuedAt(Instant.now().toEpochMilli())
            .verifiableCredential(vc)
            .build();
    String payloadStr = MAPPER.writeValueAsString(claims);
    byte[] payloadAsBytes =
        deflate ? deflate(payloadStr) : payloadStr.getBytes(StandardCharsets.UTF_8);
    Payload payload = new Payload(payloadAsBytes);
    JWSHeader header = jwsHeader(jwksProperties.currentPrivateJwk(), deflate);
    JWSObject jwsObject = new JWSObject(header, payload);
    jwsObject.sign(new ECDSASigner(jwksProperties.currentPrivateJwk().toECKey()));
    return jwsObject.serialize();
  }
}
