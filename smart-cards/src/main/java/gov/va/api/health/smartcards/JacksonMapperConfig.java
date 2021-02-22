package gov.va.api.health.smartcards;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonMapperConfig {

  public static ObjectMapper createMapper() {
    return JacksonConfig.createMapper().registerModule(new Resource.ResourceModule());
  }

  @Bean
  public ObjectMapper objectMapper() {
    return createMapper();
  }
}
