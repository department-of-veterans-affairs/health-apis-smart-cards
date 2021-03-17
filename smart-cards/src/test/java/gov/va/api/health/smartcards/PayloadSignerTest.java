package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.smartcards.vc.PayloadClaimsWrapper;
import gov.va.api.health.smartcards.vc.VerifiableCredential;
import gov.va.api.health.smartcards.vc.VerifiableCredential.CredentialSubject;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class PayloadSignerTest {
  private static final JwksProperties JWKS_PROPERTIES = JwsHelpers.jwksProperties("123");

  private static LinkProperties _linkProperties() {
    return LinkProperties.builder()
        .dqInternalUrl("http://dq.foo")
        .dqInternalR4BasePath("r4")
        .baseUrl("http://sc.bar")
        .r4BasePath("r4")
        .build();
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
  @Test
  void signAndDeflate() {
    PayloadSigner signer =
        PayloadSigner.builder()
            .jwksProperties(JWKS_PROPERTIES)
            .linkProperties(_linkProperties())
            .build();
    var vc = vc();
    String jws = signer.sign(vc, true);

    // Verify signature with public key
    assertThat(JwsHelpers.verify(jws, JWKS_PROPERTIES.currentPublicJwk())).isTrue();
    assertThat(JwsHelpers.verify(jws, JwsHelpers.genEcJwk("random").toPublicJWK())).isFalse();
    JWSObject jwsObject = JWSObject.parse(jws);

    // Verify header
    JWSHeader jwsHeader = jwsObject.getHeader();
    assertThat(jwsHeader.getCustomParam("zip")).isEqualTo("DEF");
    assertThat(jwsHeader.getKeyID()).isEqualTo(JWKS_PROPERTIES.currentKeyId());

    // Verify payload
    String inflated = JwsHelpers.decompress(jwsObject.getPayload().toBytes());
    PayloadClaimsWrapper claims =
        JacksonMapperConfig.createMapper().readValue(inflated, PayloadClaimsWrapper.class);
    assertThat(claims.verifiableCredential()).isEqualTo(vc);
  }

  @SneakyThrows
  @Test
  void signWithoutCompressions() {
    PayloadSigner signer =
        PayloadSigner.builder()
            .jwksProperties(JWKS_PROPERTIES)
            .linkProperties(_linkProperties())
            .build();
    var vc = vc();
    String jws = signer.sign(vc, false);

    // Verify signature with public key
    assertThat(JwsHelpers.verify(jws, JWKS_PROPERTIES.currentPublicJwk())).isTrue();
    assertThat(JwsHelpers.verify(jws, JwsHelpers.genEcJwk("random").toPublicJWK())).isFalse();
    JWSObject jwsObject = JWSObject.parse(jws);

    // Verify header
    JWSHeader jwsHeader = jwsObject.getHeader();
    assertThat(jwsHeader.getCustomParam("zip")).isNull();
    assertThat(jwsHeader.getKeyID()).isEqualTo(JWKS_PROPERTIES.currentKeyId());

    // Verify payload
    PayloadClaimsWrapper claims =
        JacksonMapperConfig.createMapper()
            .readValue(jwsObject.getPayload().toString(), PayloadClaimsWrapper.class);
    assertThat(claims.verifiableCredential()).isEqualTo(vc);
  }
}
