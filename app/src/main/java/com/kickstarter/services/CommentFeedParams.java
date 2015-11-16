package com.kickstarter.services;

import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.Project;
import com.kickstarter.models.Update;

import java.util.HashMap;
import java.util.Map;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class CommentFeedParams implements Parcelable {
  public abstract @Nullable String cursor();
  public abstract @Nullable String since();
  public abstract Project project();
  public abstract @Nullable Update update();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder cursor(@Nullable String __);
    public abstract Builder since(@Nullable String __);
    public abstract Builder project(Project __);
    public abstract Builder update(@Nullable Update __);
    public abstract CommentFeedParams build();
  }

  public static Builder builder() {
    return new AutoParcel_CommentFeedParams.Builder()
      .cursor(null)
      .since(null);
  }

  public abstract Builder toBuilder();

  public @NonNull CommentFeedParams nextPageFromUrl(final @NonNull String url) {
    Uri uri = Uri.parse(url);
    final String cursor = uri.getQueryParameter("cursor");
    final String since = uri.getQueryParameter("since");

    return toBuilder()
      .cursor(cursor)
      .since(since)
      .build();
  }

  @NonNull public Map<String, String> paginationParams() {
    return new HashMap<String, String>() {{
      put("cursor", cursor());
      put("since", since());
    }};
  }
}
