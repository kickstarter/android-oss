package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.Comment;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class CommentsEnvelope implements Parcelable {
  public abstract List<Comment> comments();
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
        return new AutoParcel_CommentsEnvelope_UrlsEnvelope_ApiEnvelope.Builder();
      }
    }

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder api(ApiEnvelope __);
      public abstract UrlsEnvelope build();
    }

    public static Builder builder() {
      return new AutoParcel_CommentsEnvelope_UrlsEnvelope.Builder();
    }
  }

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder comments(List<Comment> __);
    public abstract Builder urls(UrlsEnvelope __);
    public abstract CommentsEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_CommentsEnvelope.Builder();
  }

  public abstract Builder toBuilder();
}
