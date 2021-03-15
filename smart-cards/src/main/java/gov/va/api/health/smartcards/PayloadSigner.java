package gov.va.api.health.smartcards;

import static com.google.common.base.Preconditions.checkState;

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
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class PayloadSigner {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  private final JWK jwkPrivate;

  private final LinkProperties linkProperties;

  @Builder
  @SneakyThrows
  PayloadSigner(
      @Value("${payload-signer.jwk-private}") String jwkPrivateJson,
      LinkProperties linkProperties) {
    checkState(!"unset".equals(jwkPrivateJson), "payload-signer.jwk-private is unset");
    this.jwkPrivate = JWK.parse(jwkPrivateJson);
    this.linkProperties = linkProperties;
  }

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
    JWSHeader header = jwsHeader(jwkPrivate, deflate);
    JWSObject jwsObject = new JWSObject(header, payload);
    jwsObject.sign(new ECDSASigner(jwkPrivate.toECKey()));
    return jwsObject.serialize();
  }
}
