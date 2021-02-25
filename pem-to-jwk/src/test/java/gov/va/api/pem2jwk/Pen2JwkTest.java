package gov.va.api.pem2jwk;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nimbusds.jose.JOSEException;
import java.io.FileNotFoundException;
import org.junit.jupiter.api.Test;

public class Pen2JwkTest {

  private String[] args(String... args) {
    return args;
  }

  @Test
  public void test() {
    Pem2Jwk.main(args("./src/test/resources/cert.pem"));
  }

  @Test
  public void test_invalid() {
    // no such file
    assertThrows(
        FileNotFoundException.class, () -> Pem2Jwk.main(args("./not/a/real/file/path.txt")));

    // invalid pem
    assertThrows(JOSEException.class, () -> Pem2Jwk.main(args("./src/test/resources/invalid.pem")));

    // no args
    assertThrows(IllegalArgumentException.class, () -> Pem2Jwk.main(args()));
    // too many args
    assertThrows(IllegalArgumentException.class, () -> Pem2Jwk.main(args("x", "y")));
  }
}
