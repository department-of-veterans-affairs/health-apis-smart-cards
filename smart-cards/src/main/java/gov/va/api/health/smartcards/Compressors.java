package gov.va.api.health.smartcards;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class Compressors {
  private static final int BUFFER_SIZE = 1024;

  /** Compress a payload with Java's ZLIB implementation. */
  @SneakyThrows
  public static byte[] compress(@NonNull byte[] input) {
    Deflater deflater = new Deflater(Deflater.DEFLATED, true);
    deflater.setInput(input);
    deflater.finish();

    byte[] buffer = new byte[BUFFER_SIZE];
    byte[] result;
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      while (!deflater.finished()) {
        var deflated = deflater.deflate(buffer);
        bos.write(buffer, 0, deflated);
      }
      result = bos.toByteArray();
    } finally {
      deflater.end();
    }
    return result;
  }

  /** Decompress a payload with Java's ZLIB implementation. */
  @SneakyThrows
  public static byte[] uncompress(@NonNull byte[] input) {
    Inflater inflater = new Inflater(true);
    inflater.setInput(input);
    byte[] buffer = new byte[BUFFER_SIZE];
    byte[] result;
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      while (!inflater.finished()) {
        int inflated = inflater.inflate(buffer);
        bos.write(buffer, 0, inflated);
      }
      result = bos.toByteArray();
    } finally {
      inflater.end();
    }
    return result;
  }
}
