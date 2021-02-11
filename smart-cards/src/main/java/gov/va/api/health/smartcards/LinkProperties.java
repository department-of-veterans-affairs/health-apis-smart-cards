package gov.va.api.health.smartcards;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import gov.va.api.health.r4.api.resources.Resource;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class LinkProperties {

  private final String r4Url;

  @Builder
  @Autowired
  LinkProperties(
      @Value("${public-url}") String baseUrl, @Value("${public-r4-base-path}") String r4BasePath) {

    checkState(!"unset".equals(baseUrl), "public-url is unset");
    checkState(!"unset".equals(r4BasePath), "public-r4-base-path is unset");
    String stripUrl = baseUrl.replaceAll("/$", "");
    checkState(isNotBlank(stripUrl), "public-url is blank");
    String stripR4 = r4BasePath.replaceAll("^/", "").replaceAll("/$", "");
    String combined = stripUrl;
    if (!stripR4.isEmpty()) {
      combined += "/" + stripR4;
    }
    r4Url = combined;
  }

  public String r4ReadUrl(Resource resource) {
    String name = resource.getClass().getSimpleName();
    return r4ResourceUrl(name) + "/" + resource.id();
  }

  public String r4ResourceUrl(String resource) {
    return r4Url + "/" + resource;
  }
}
