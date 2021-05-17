package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.DeprecatedComment;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class DeprecatedCommentsEnvelope implements Parcelable {
  public abstract List<DeprecatedComment> comments();
  public abstract UrlsEnvelope urls();

  @AutoGson
  @AutoParcel
  public abstract static class UrlsEnvelope implements Parcelable {
    public abstract ApiEnvelope api();

    @AutoGson
    @AutoParcel
    public abstract static class ApiEnvelope implements Parcelable {
      public abstract String moreComments();
      public abstract String newerComments();

      @AutoParcel.Builder
      public abstract static class Builder {
        public abstract Builder moreComments(String __);
        public abstract Builder newerComments(String __);
        public abstract ApiEnvelope build();
      }

      public static Builder builder() {
        return new AutoParcel_DeprecatedCommentsEnvelope_UrlsEnvelope_ApiEnvelope.Builder();
      }
    }

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder api(ApiEnvelope __);
      public abstract UrlsEnvelope build();
    }

    public static Builder builder() {
      return new AutoParcel_DeprecatedCommentsEnvelope_UrlsEnvelope.Builder();
    }
  }

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder comments(List<DeprecatedComment> __);
    public abstract Builder urls(UrlsEnvelope __);
    public abstract DeprecatedCommentsEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_DeprecatedCommentsEnvelope.Builder();
  }

  public abstract Builder toBuilder();
}
