package gov.va.api.health.smartcards.mockservices;

import static com.google.common.base.Preconditions.checkState;
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
import org.mockserver.mockserver.MockServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MockServices {
  private final List<String> supportedQueries = new ArrayList<>();

  MockServicesOptions options;

  private MockServer ms;

  private void addDummyTestResponse(MockServerClient mock) {
    mock.when(addQuery("/fhir/v0/r4/Patient?_id=1011537977V693883"))
        .respond(
            response()
                .withStatusCode(200)
                .withHeader(contentApplicationJson())
                .withBody(contentOf("/sandbox-response.json")));
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

  private Header contentApplicationJson() {
    return new Header("Content-Type", "application/json");
  }

  @SneakyThrows
  private String contentOf(String resource) {
    log.info("Loading resource {}", resource);
    return Resources.toString(getClass().getResource(resource), StandardCharsets.UTF_8);
  }

  /** Inits server. */
  public void start() {
    options = new MockServicesOptions();
    checkState(ms == null, "Mock Services have already been started");
    log.info("Starting mock services on port {}", options.getPort());
    ms = new MockServer(options.getPort());
    MockServerClient mock = new MockServerClient("localhost", options.getPort());
    addDummyTestResponse(mock);
  }
}
