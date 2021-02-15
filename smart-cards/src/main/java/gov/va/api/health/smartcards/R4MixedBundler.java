package gov.va.api.health.smartcards;

import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.r4.api.bundle.AbstractEntry.Search;
import gov.va.api.health.r4.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.resources.Resource;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class R4MixedBundler {

  private final LinkProperties linkProperties;

  private MixedEntry asEntry(Resource resource) {
    return MixedEntry.builder()
        .resource(resource)
        .fullUrl(linkProperties.r4ReadUrl(resource))
        .search(Search.builder().mode(SearchMode.match).build())
        .build();
  }

  /** Bundle. */
  public MixedBundle bundle(List<Resource> resources) {
    var bundle =
        MixedBundle.builder()
            .resourceType("Bundle")
            .type(BundleType.collection)
            .total(resources.size())
            .entry(
                resources.stream()
                    .map(
                        t -> {
                          return asEntry(t);
                        })
                    .collect(toList()))
            .build();
    return bundle;
  }
}
