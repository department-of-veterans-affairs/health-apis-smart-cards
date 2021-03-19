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
public class Dstu2OpenApiController {
  private static final String OPEN_API = initOpenApi();

  @SneakyThrows
  private static String initOpenApi() {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(mapper.readTree(loadOpenApi()));
  }

  @ResponseBody
  @SneakyThrows
  private static String loadOpenApi() {
    try (InputStream is = new ClassPathResource("dstu2-openapi.json").getInputStream()) {
      return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
    }
  }

  @ResponseBody
  @GetMapping(
      value = {"/dstu2", "/dstu2/openapi.json"},
      produces = "application/json")
  public String openApi() {
    return OPEN_API;
  }
}
