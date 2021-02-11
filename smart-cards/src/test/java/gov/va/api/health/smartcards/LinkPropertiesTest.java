package gov.va.api.health.smartcards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.va.api.health.r4.api.resources.Immunization;
import org.junit.jupiter.api.Test;

public class LinkPropertiesTest {
  private static void verifyUrl(String url, String path, String expected) {
    LinkProperties pageLinks = LinkProperties.builder().baseUrl(url).r4BasePath(path).build();
    assertThat(pageLinks.r4Url()).isEqualTo(expected);
  }

  @Test
  void buildUrl() {
    verifyUrl("http://va.gov", "", "http://va.gov");
    verifyUrl("http://va.gov/", "", "http://va.gov");

    verifyUrl("http://va.gov", "/", "http://va.gov");
    verifyUrl("http://va.gov/", "/", "http://va.gov");

    verifyUrl("http://va.gov", "//", "http://va.gov");
    verifyUrl("http://va.gov/", "//", "http://va.gov");

    verifyUrl("http://va.gov", "api/gov", "http://va.gov/api/gov");
    verifyUrl("http://va.gov/", "api/gov", "http://va.gov/api/gov");

    verifyUrl("http://va.gov", "/api/gov", "http://va.gov/api/gov");
    verifyUrl("http://va.gov/", "/api/gov", "http://va.gov/api/gov");

    verifyUrl("http://va.gov", "api/gov/", "http://va.gov/api/gov");
    verifyUrl("http://va.gov/", "api/gov/", "http://va.gov/api/gov");

    verifyUrl("http://va.gov", "/api/gov/", "http://va.gov/api/gov");
    verifyUrl("http://va.gov/", "/api/gov/", "http://va.gov/api/gov");
  }

  @Test
  void invalidBaseUrl() {
    assertThrows(
        IllegalStateException.class,
        () -> LinkProperties.builder().baseUrl("").r4BasePath("x").build());
    assertThrows(
        IllegalStateException.class,
        () -> LinkProperties.builder().baseUrl("/").r4BasePath("x").build());
  }

  @Test
  void resourceUrls() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://va.gov").r4BasePath("api/r4").build();

    assertThat(pageLinks.r4ResourceUrl("Immunization"))
        .isEqualTo("http://va.gov/api/r4/Immunization");

    assertThat(pageLinks.r4ReadUrl(Immunization.builder().id("123").build()))
        .isEqualTo("http://va.gov/api/r4/Immunization/123");
  }

  @Test
  void unset() {
    assertThrows(
        IllegalStateException.class,
        () -> LinkProperties.builder().baseUrl("unset").r4BasePath("x").build());
    assertThrows(
        IllegalStateException.class,
        () -> LinkProperties.builder().baseUrl("x").r4BasePath("unset").build());
    assertThrows(
        IllegalStateException.class,
        () -> LinkProperties.builder().baseUrl("unset").r4BasePath("unset").build());
  }
}
