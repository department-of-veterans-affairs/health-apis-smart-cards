package gov.va.api.pem2jwk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import java.io.File;
import java.nio.charset.Charset;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class Pem2JwkConverterTest {

  @Test
  @SneakyThrows
  public void test() {
    String contents =
        FileUtils.readFileToString(
            new File("src/test/resources/cert.txt"), Charset.defaultCharset());

    var out = Pem2JwkConverter.builder().pemContents(contents).build().toJwk();
    assertThat(out.type()).isEqualTo(KeyType.EC);
    assertNull(out.use());
    assertThat(out.thumb().toString()).isEqualTo("J6TZomqH6LxXoG_-gMJZioVaisXCVaLI8fxElW8g-OE");
    // No private keys so toJson and toPublic().toJson() should match
    assertThat(out.jwk()).isEqualTo(out.jwk().toPublicJWK());

    // Filled jwk should include some extra fields
    assertThat(out.jwkFilled().getAlgorithm().toString()).isEqualTo("ES256");
    assertThat(out.jwkFilled().getKeyID()).isEqualTo(out.thumb().toString());
    assertThat(out.jwkFilled().getKeyUse()).isEqualTo(KeyUse.SIGNATURE);
  }

  @Test
  public void test_invalid() {
    assertThrows(
        JOSEException.class, () -> Pem2JwkConverter.builder().pemContents("NOPE").build().toJwk());
  }
}
