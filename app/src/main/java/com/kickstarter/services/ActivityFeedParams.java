package com.kickstarter.services;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.Activity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ActivityFeedParams {
  @Nullable final Integer cursor;
  @Nullable final Integer since;

  public ActivityFeedParams() {
    cursor = null;
    since = null;
  }

  public ActivityFeedParams(@Nullable final Integer cursor, @Nullable final Integer since) {
    this.cursor = null;
    this.since = null;
  }

  @NonNull public static ActivityFeedParams cursor(@NonNull final Integer cursor) {
    return new ActivityFeedParams(cursor, null);
  }

  @NonNull public static ActivityFeedParams since(@NonNull final Integer since) {
    return new ActivityFeedParams(null, since);
  }

  @NonNull public static ActivityFeedParams fromUrl(@NonNull final String url) {

    Uri uri = Uri.parse(url);
    final String cursorString = uri.getQueryParameter("cursor");
    final String sinceString = uri.getQueryParameter("since");
    Integer cursor = null;
    Integer since = null;
    if (cursorString != null) {
      cursor = Integer.valueOf(cursorString);
    }
    if (sinceString != null) {
      since = Integer.valueOf(sinceString);
    }

    return new ActivityFeedParams(cursor, since);
  }

  @NonNull public List<String> categoryParams() {
    return Arrays.asList(
      Activity.CATEGORY_BACKING,
      Activity.CATEGORY_CANCELLATION,
      Activity.CATEGORY_FAILURE,
      Activity.CATEGORY_LAUNCH,
      Activity.CATEGORY_SUCCESS,
      Activity.CATEGORY_UPDATE,
      Activity.CATEGORY_FOLLOW
    );
  }

  @NonNull public Map<String, String> paginationParams() {
    return Collections.unmodifiableMap(new HashMap<String, String>() {{

      if (cursor != null) {
        put("cursor", String.valueOf(cursor));
      }
      if (since != null) {
        put("since", String.valueOf(since));
      }

    }});
  }
}
