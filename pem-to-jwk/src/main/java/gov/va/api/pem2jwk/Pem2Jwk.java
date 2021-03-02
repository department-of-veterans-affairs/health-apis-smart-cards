package gov.va.api.pem2jwk;

import gov.va.api.pem2jwk.Pem2JwkConverter.JwkOutput;
import java.io.File;
import java.nio.charset.Charset;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

@Slf4j
public class Pem2Jwk {

  /** Main CLI entrypoint. */
  @SneakyThrows
  public static final void main(String[] args) {
    if (args.length != 1) {
      throw new IllegalArgumentException("Invalid number of args. Only input pem file is allowed");
    }
    String input = args[0];
    String contents = FileUtils.readFileToString(new File(input), "UTF-8");
    JwkOutput converted = Pem2JwkConverter.builder().pemContents(contents).build().toJwk();
    log.info("From " + input + ": ");
    log.info("Type                  : " + converted.type());
    log.info("Use                   : " + converted.use());
    log.info("Thumb                 : " + converted.thumb().toString());
    log.info("Input to json         : " + converted.jwk().toJSONString());
    log.info("Input to PUBLIC json  : " + converted.jwk().toPublicJWK().toJSONString());
    log.info("Output to json        : " + converted.jwkFilled().toJSONString());
    log.info("Output to PUBLIC json : " + converted.jwkFilled().toPublicJWK().toJSONString());
  }
}
