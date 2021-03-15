package gov.va.api.health.smartcards;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Compressors {
  private static final int BUFFER_SIZE = 1024;

  /** Compress a payload with Java's ZLIB implementation. */
  public static byte[] deflate(byte[] input) {
    checkNotNull(input);
    Deflater deflater = new Deflater();
    deflater.setInput(input);
    deflater.finish();

    byte[] buffer = new byte[BUFFER_SIZE];
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      while (!deflater.finished()) {
        var deflated = deflater.deflate(buffer);
        bos.write(buffer, 0, deflated);
      }
    } finally {
      deflater.end();
    }
    return bos.toByteArray();
  }

  /** Decompress a payload with Java's ZLIB implementation. */
  @SneakyThrows
  public static byte[] inflate(byte[] input) {
    checkNotNull(input);
    Inflater inflater = new Inflater();
    inflater.setInput(input);
    byte[] buffer = new byte[BUFFER_SIZE];
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      while (!inflater.finished()) {
        int inflated = inflater.inflate(buffer);
        bos.write(buffer, 0, inflated);
      }
    } finally {
      inflater.end();
    }
    return bos.toByteArray();
  }
}
