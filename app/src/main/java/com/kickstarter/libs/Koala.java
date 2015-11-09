package com.kickstarter.libs;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kickstarter.libs.utils.MapUtils;
import com.kickstarter.models.Category;
import com.kickstarter.services.DiscoveryParams;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Koala {
  @NonNull private final MixpanelAPI client;

  public Koala(@ForApplication @NonNull final Context context) {
    client = MixpanelAPI.getInstance(context, "koala");
  }

  public void trackDiscovery(@NonNull final DiscoveryParams params) {
    client.trackMap("Discover List View", discoveryParamsProperties(params));
  }

  public void trackDiscoveryFilters() {
    client.trackMap("Discover Switch Modal", new HashMap<String, Object>(){{
      put("modal_type", "filters");
    }});
  }

  public void trackDiscoveryFilterSelected(@NonNull final DiscoveryParams params) {
    client.trackMap("Discover Modal Selected Filter", discoveryParamsProperties(params));
  }

  @NonNull private static Map<String, Object> discoveryParamsProperties(@NonNull final DiscoveryParams params) {

    final Map<String, Object> properties = Collections.unmodifiableMap(new HashMap<String, Object>(){{

      put("staff_picks", String.valueOf(params.staffPicks()));
      put("sort", params.sort().toString());
      put("page", String.valueOf(params.page()));
      put("per_page", String.valueOf(params.perPage()));

      Category category = params.category();
      if (category != null) {
        putAll(categoryProperties(category));
      }

    }});

    return MapUtils.prefixKeys(properties, "discover_");
  }

  @NonNull private static Map<String, Object> categoryProperties(@NonNull final Category category) {
    return Collections.unmodifiableMap(new HashMap<String, Object>() {{
      put("category_id", String.valueOf(category.id()));
      put("category_name", String.valueOf(category.name()));
    }});
  }
}
