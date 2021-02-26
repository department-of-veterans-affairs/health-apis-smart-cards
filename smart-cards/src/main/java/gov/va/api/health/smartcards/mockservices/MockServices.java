package gov.va.api.health.smartcards.mockservices;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MockServices {

    private final List<String> supportedQueries = new ArrayList<>();

    @Autowired MockServicesOptions options;

    @SneakyThrows
    private HttpRequest addQuery(String path) {
        log.info("http://localhost:{}{}", options.getPort(), path);
        supportedQueries.add("http://localhost:" + options.getPort() + path);
        URL url = new URL("http://localhost:" + path);
//        HttpRequest request = request().withPath(url.getPath());
        return null;
    }

}
