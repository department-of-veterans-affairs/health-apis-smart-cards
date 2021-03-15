package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.smartcards.vc.PayloadClaimsWrapper;
import gov.va.api.health.smartcards.vc.VerifiableCredential;
import gov.va.api.health.smartcards.vc.VerifiableCredential.CredentialSubject;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class PayloadSignerTest {
  private static final String JWK_PRIVATE = genEcJwk("123").toJSONString();

  private static LinkProperties _linkProperties() {
    return LinkProperties.builder()
        .dqInternalUrl("http://dq.foo")
        .dqInternalR4BasePath("r4")
        .baseUrl("http://sc.bar")
        .r4BasePath("r4")
        .build();
  }

  @SneakyThrows
  private static ECKey genEcJwk(String kid) {
    return new ECKeyGenerator(Curve.P_256).keyID(kid).generate();
  }

  private static VerifiableCredential vc() {
    return VerifiableCredential.builder()
        .context(List.of("https://www.w3.org/2018/credentials/v1"))
        .type(List.of("VerifiableCredential", "https://smarthealth.cards#covid19"))
        .credentialSubject(
            CredentialSubject.builder()
                .fhirBundle(
                    MixedBundle.builder()
                        .resourceType("Bundle")
                        .type(BundleType.collection)
                        .total(1)
                        .entry(
                            List.of(
                                MixedEntry.builder()
                                    .fullUrl("resource:0")
                                    .resource(
                                        Patient.builder()
                                            .name(
                                                List.of(HumanName.builder().family("Doe").build()))
                                            .build())
                                    .build()))
                        .build())
                .build())
        .build();
  }

  @SneakyThrows
  private String decompress(byte[] input) {
    return new String(Compressors.inflate(input), StandardCharsets.UTF_8);
  }

  @SneakyThrows
  @Test
  void signAndDeflate() {
    PayloadSigner signer = new PayloadSigner(JWK_PRIVATE, _linkProperties());
    var vc = vc();
    String jws = signer.sign(vc, true);

    // Verify signature with public key
    assertThat(verify(jws)).isTrue();
    assertThat(verify(jws, genEcJwk("random").toPublicJWK())).isFalse();
    JWSObject jwsObject = JWSObject.parse(jws);

    // Verify header
    JWSHeader jwsHeader = jwsObject.getHeader();
    assertThat(jwsHeader.getCustomParam("zip")).isEqualTo("DEF");
    assertThat(jwsHeader.getKeyID()).isEqualTo(JWK.parse(JWK_PRIVATE).getKeyID());

    // Verify payload
    String inflated = decompress(jwsObject.getPayload().toBytes());
    PayloadClaimsWrapper claims =
        JacksonMapperConfig.createMapper().readValue(inflated, PayloadClaimsWrapper.class);
    assertThat(claims.verifiableCredential()).isEqualTo(vc);
  }

  @SneakyThrows
  @Test
  void signWithoutCompressions() {
    PayloadSigner signer = new PayloadSigner(JWK_PRIVATE, _linkProperties());
    var vc = vc();
    String jws = signer.sign(vc, false);

    // Verify signature with public key
    assertThat(verify(jws)).isTrue();
    assertThat(verify(jws, genEcJwk("random").toPublicJWK())).isFalse();
    JWSObject jwsObject = JWSObject.parse(jws);

    // Verify header
    JWSHeader jwsHeader = jwsObject.getHeader();
    assertThat(jwsHeader.getCustomParam("zip")).isNull();
    assertThat(jwsHeader.getKeyID()).isEqualTo(JWK.parse(JWK_PRIVATE).getKeyID());

    // Verify payload
    PayloadClaimsWrapper claims =
        JacksonMapperConfig.createMapper()
            .readValue(jwsObject.getPayload().toString(), PayloadClaimsWrapper.class);
    assertThat(claims.verifiableCredential()).isEqualTo(vc);
  }

  @SneakyThrows
  private boolean verify(String jws) {
    JWK jwkPrivate = JWK.parse(JWK_PRIVATE);
    return verify(jws, jwkPrivate.toPublicJWK());
  }

  @SneakyThrows
  private boolean verify(String jws, JWK publicJwk) {
    JWSVerifier verifier = new ECDSAVerifier(publicJwk.toECKey());
    JWSObject jwsObject = JWSObject.parse(jws);
    return jwsObject.verify(verifier);
  }
}
