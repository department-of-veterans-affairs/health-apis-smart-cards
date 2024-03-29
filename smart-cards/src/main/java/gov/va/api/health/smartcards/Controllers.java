package gov.va.api.health.smartcards;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.base.Splitter;
import gov.va.api.health.r4.api.elements.Reference;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public final class Controllers {
  public static boolean parseBooleanOrTrue(String value) {
    // not using parseBoolean because we need to default to true
    return !"false".equalsIgnoreCase(StringUtils.trimToEmpty(value));
  }

  /**
   * Extract resource ID from a reference. This is looking for any number of path elements, then a
   * resource type followed by an ID, e.g. `foo/bar/Patient/1234567890V123456`.
   */
  public static String resourceId(Reference ref) {
    if (ref == null) {
      return null;
    }
    return resourceId(ref.reference());
  }

  /**
   * Extract resource ID from a string. This is looking for any number of path elements, then a
   * resource type followed by an ID, e.g. `foo/bar/Patient/1234567890V123456`.
   */
  public static String resourceId(String str) {
    if (str == null || isBlank(str)) {
      return null;
    }
    List<String> splitReference = Splitter.on('/').trimResults().splitToList(str);
    if (splitReference.size() <= 1) {
      return null;
    }
    if (isBlank(splitReference.get(splitReference.size() - 2))) {
      return null;
    }
    String resourceId = splitReference.get(splitReference.size() - 1);
    if (isBlank(resourceId)) {
      return null;
    }
    return resourceId;
  }
}
