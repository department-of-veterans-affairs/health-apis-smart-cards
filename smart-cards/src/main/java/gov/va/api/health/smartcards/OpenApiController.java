package gov.va.api.health.smartcards;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OpenApiController {
  private static final String DSTU2_OPEN_API = initDstu2OpenApi();

  private static final String R4_OPEN_API = initR4OpenApi();

  @SneakyThrows
  private static String initDstu2OpenApi() {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(mapper.readTree(loadDstu2OpenApi()));
  }

  @SneakyThrows
  private static String initR4OpenApi() {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(mapper.readTree(loadR4OpenApi()));
  }

  @SneakyThrows
  private static String loadDstu2OpenApi() {
    try (InputStream is = new ClassPathResource("dstu2-openapi.json").getInputStream()) {
      return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
    }
  }

  @SneakyThrows
  private static String loadR4OpenApi() {
    try (InputStream is = new ClassPathResource("r4-openapi.json").getInputStream()) {
      return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
    }
  }

  @ResponseBody
  @GetMapping(
      value = {"/dstu2", "/dstu2/openapi.json"},
      produces = "application/json")
  String dstu2() {
    return DSTU2_OPEN_API;
  }

  @ResponseBody
  @GetMapping(
      value = {"/r4", "/r4/openapi.json"},
      produces = "application/json")
  String r4() {
    return R4_OPEN_API;
  }
}
