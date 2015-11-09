package com.kickstarter.services;

import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.Activity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class ActivityFeedParams implements Parcelable {
  abstract @Nullable Integer cursor();
  abstract @Nullable Integer since();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder cursor(Integer __);
    public abstract Builder since(Integer __);
    public abstract ActivityFeedParams build();
  }

  public static Builder builder() {
    return new AutoParcel_ActivityFeedParams.Builder()
      .cursor(null)
      .since(null);
  }

  public abstract Builder toBuilder();

  public static @NonNull ActivityFeedParams fromUrl(final @NonNull String url) {

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

    return ActivityFeedParams.builder()
      .cursor(cursor)
      .since(since)
      .build();
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

      if (cursor() != null) {
        put("cursor", String.valueOf(cursor()));
      }
      if (since() != null) {
        put("since", String.valueOf(since()));
      }

    }});
  }
}
