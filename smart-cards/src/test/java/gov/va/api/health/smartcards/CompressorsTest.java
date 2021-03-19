package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import org.junit.jupiter.api.Test;

public class CompressorsTest {
  @Test
  void badDataFormat() {
    String contents = "pteracuda";
    byte[] notActuallyCompressed = contents.getBytes(StandardCharsets.UTF_8);
    assertThrows(DataFormatException.class, () -> Compressors.uncompress(notActuallyCompressed));
  }

  @Test
  void compressAndUncompress() {
    String contents = "a very long input to compress";
    byte[] compressed = Compressors.compress(contents.getBytes(StandardCharsets.UTF_8));
    byte[] uncompressed = Compressors.uncompress(compressed);
    assertThat(new String(uncompressed, StandardCharsets.UTF_8)).isEqualTo(contents);
  }
}
