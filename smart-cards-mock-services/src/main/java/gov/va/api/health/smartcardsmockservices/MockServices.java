package gov.va.api.health.smartcardsmockservices;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.joining;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.google.common.io.Resources;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.netty.MockServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockServices {
  private final List<String> supportedQueries = new ArrayList<>();

  @Autowired private MockServicesOptions options;

  private MockServer ms;

  private static Header contentApplicationJson() {
    return new Header("Content-Type", "application/json");
  }

  @SneakyThrows
  private static String contentOf(String resource) {
    log.info("Loading resource {}", resource);
    return Resources.toString(MockServices.class.getResource(resource), StandardCharsets.UTF_8);
  }

  private static Header contentTextPlain() {
    return new Header("Content-Type", "text/plain");
  }

  private void addHelp(MockServerClient mock) {
    mock.when(request().withPath("/help"))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentTextPlain())
                .withBody(supportedQueries.stream().sorted().collect(joining("\n"))));
    log.info("List of supported queries available at http://localhost:{}/help", options.getPort());
  }

  private void addImmunizationBundle(MockServerClient mock) {
    mock.when(addQuery("/Immunization?patient=1011537977V693883"))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentApplicationJson())
                .withBody(contentOf("/immunization-bundle.json")));
  }

  private void addPatientBundle(MockServerClient mock) {
    mock.when(addQuery("/Patient?_id=1011537977V693883"))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentApplicationJson())
                .withBody(contentOf("/patient-bundle.json")));
  }

  @SneakyThrows
  private HttpRequest addQuery(String path) {
    log.info("http://localhost:{}{}", options.getPort(), path);
    supportedQueries.add("http://localhost:" + options.getPort() + path);
    URL url = new URL("http://localhost" + path);
    HttpRequest request = request().withPath(url.getPath());
    if (url.getQuery() == null) {
      return request;
    }
    Stream.of(url.getQuery().split("&"))
        .forEach(
            q -> {
              var pv = q.split("=", 2);
              request.withQueryStringParameter(
                  pv[0], URLDecoder.decode(pv[1], StandardCharsets.UTF_8));
            });
    return request;
  }

  void start() {
    checkState(ms == null, "Mock Services have already been started");
    log.info("Starting mock services on port {}", options.getPort());
    ms = new MockServer(options.getPort());
    MockServerClient mock = new MockServerClient("localhost", options.getPort());
    addImmunizationBundle(mock);
    addPatientBundle(mock);
    addHelp(mock);
  }
}
