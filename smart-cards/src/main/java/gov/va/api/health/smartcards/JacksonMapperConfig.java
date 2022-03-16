package gov.va.api.health.smartcards;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonMapperConfig {
  public static ObjectMapper createMapper() {
    return JacksonConfig.createMapper();
  }

  @Bean
  ObjectMapper objectMapper() {
    return createMapper();
  }
}
