package com.kickstarter.services;

import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.Activity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class ActivityFeedParams implements Parcelable {
  abstract @Nullable String cursor();
  abstract @Nullable String since();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder cursor(@Nullable String __);
    public abstract Builder since(@Nullable String __);
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
    final String cursor= uri.getQueryParameter("cursor");
    final String since= uri.getQueryParameter("since");

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
    return new HashMap<String, String>() {{
      put("cursor", cursor());
      put("since", since());
    }};
  }
}
