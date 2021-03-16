package gov.va.api.health.smartcards;

import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class JsonPropertyLogger {

  @Value("${jwks-single-quotes:}")
  String jwksSingleQuotes;

  @Value("${jwks-double-quotes:}")
  String jwksDoubleQuotes;

  @Value("${jwks-no-quotes:}")
  String jwksNoQuotes;

  @Value("${jwks-no-quotes-escaped:}")
  String jwksNoQuotesEscaped;

  @PostConstruct
  void init() {
    log.warn("Single Quotes: {}", jwksSingleQuotes);
    log.warn("Double Quotes: {}", jwksDoubleQuotes);
    log.warn("No Quotes    : {}", jwksNoQuotes);
    log.warn("No Quotes Esc: {}", jwksNoQuotesEscaped);
  }
}
