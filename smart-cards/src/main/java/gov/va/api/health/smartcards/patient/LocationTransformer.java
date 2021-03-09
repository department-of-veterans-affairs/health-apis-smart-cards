package gov.va.api.health.smartcards.patient;

import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.MixedEntry;
import gov.va.api.health.r4.api.resources.Location;
import lombok.Builder;

@Builder
public class LocationTransformer {
  Location.Entry entry;

  MixedEntry transform() {
    return MixedEntry.builder()
        .fullUrl(entry.fullUrl())
        .resource(
            Location.builder()
                .name(entry.resource().name())
                .address(entry.resource().address())
                .build())
        .search(AbstractEntry.Search.builder().mode(AbstractEntry.SearchMode.match).build())
        .build();
  }
}
