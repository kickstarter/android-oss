package com.kickstarter.models;

import android.os.Parcelable;
import android.text.Html;

import com.kickstarter.libs.qualifiers.AutoGson;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Update implements Parcelable {
  public abstract @Nullable String body();
  public abstract @Nullable Integer commentsCount();
  public abstract @Nullable Boolean hasLiked();
  public abstract long id();
  public abstract @Nullable Integer likesCount();
  public abstract long projectId();
  public abstract @Nullable DateTime publishedAt();
  public abstract int sequence();
  public abstract String title();
  public abstract @Nullable DateTime updatedAt();
  public abstract Urls urls();
  public abstract @Nullable User user();
  public abstract @Nullable Boolean visible();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder body(String __);
    public abstract Builder commentsCount(Integer __);
    public abstract Builder hasLiked(Boolean __);
    public abstract Builder id(long __);
    public abstract Builder likesCount(Integer __);
    public abstract Builder projectId(long __);
    public abstract Builder publishedAt(DateTime __);
    public abstract Builder sequence(int __);
    public abstract Builder title(String __);
    public abstract Builder updatedAt(DateTime __);
    public abstract Builder urls(Urls __);
    public abstract Builder user(User __);
    public abstract Builder visible(Boolean __);
    public abstract Update build();
  }

  public static Builder builder() {
    return new AutoParcel_Update.Builder();
  }

  public abstract Builder toBuilder();

  @AutoParcel
  @AutoGson
  public abstract static class Urls implements Parcelable {
    public abstract Web web();
    public abstract @Nullable Api api();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder web(Web __);
      public abstract Builder api(Api __);
      public abstract Urls build();
    }

    public static Builder builder() {
      return new AutoParcel_Update_Urls.Builder();
    }

    public abstract Builder toBuilder();

    @AutoParcel
    @AutoGson
    public abstract static class Web implements Parcelable {
      public abstract @Nullable String likes();
      public abstract String update();

      @AutoParcel.Builder
      public abstract static class Builder {
        public abstract Builder likes(String __);
        public abstract Builder update(String __);
        public abstract Web build();
      }

      public static Builder builder() {
        return new AutoParcel_Update_Urls_Web.Builder();
      }

      public abstract Builder toBuilder();
    }

    @AutoParcel
    @AutoGson
    public abstract static class Api implements Parcelable {
      public abstract @Nullable String comments();
      public abstract @Nullable String update();

      @AutoParcel.Builder
      public abstract static class Builder {
        public abstract Builder comments(String __);
        public abstract Builder update(String __);
        public abstract Api build();
      }

      public static Builder builder() {
        return new AutoParcel_Update_Urls_Api.Builder();
      }

      public abstract Builder toBuilder();
    }
  }

  private static final int TRUNCATED_BODY_LENGTH = 400;

  public @NonNull String truncatedBody() {
    try {
      String str = Html.fromHtml(body()).toString();
      if (str.length() > TRUNCATED_BODY_LENGTH) {
        str = str.substring(0, TRUNCATED_BODY_LENGTH - 1) + "\u2026";
      }
      return str;
    } catch (final @NonNull NullPointerException ignore) {}
    return "";
  }
}
