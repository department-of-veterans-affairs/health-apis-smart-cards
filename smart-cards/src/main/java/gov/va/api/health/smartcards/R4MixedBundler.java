package gov.va.api.health.smartcards;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.MixedBundle;
import gov.va.api.health.r4.api.bundle.MixedEntry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class R4MixedBundler {
  /** Bundle. */
  public MixedBundle bundle(List<MixedEntry> entries) {
    var bundle =
        MixedBundle.builder()
            .resourceType("Bundle")
            .type(AbstractBundle.BundleType.collection)
            .total(entries.size())
            .entry(entries)
            .build();
    return bundle;
  }
}
