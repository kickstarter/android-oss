package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.MessageThread;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class MessageThreadsEnvelope implements Parcelable {
  public abstract List<MessageThread> messageThreads();
  public abstract UrlsEnvelope urls();

  @AutoGson
  @AutoParcel
  public abstract static class UrlsEnvelope implements Parcelable {
    public abstract ApiEnvelope api();

    @AutoGson
    @AutoParcel
    public abstract static class ApiEnvelope implements Parcelable {
      public abstract String moreMessageThreads();

      @AutoParcel.Builder
      public abstract static class Builder {
        public abstract Builder moreMessageThreads(String __);
        public abstract ApiEnvelope build();
      }

      public static Builder builder() {
        return new AutoParcel_MessageThreadsEnvelope_UrlsEnvelope_ApiEnvelope.Builder();
      }
    }

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder api(ApiEnvelope __);
      public abstract UrlsEnvelope build();
    }

    public static Builder builder() {
      return new AutoParcel_MessageThreadsEnvelope_UrlsEnvelope.Builder();
    }
  }

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder messageThreads(List<MessageThread> __);
    public abstract Builder urls(UrlsEnvelope __);
    public abstract MessageThreadsEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_MessageThreadsEnvelope.Builder();
  }

  public abstract Builder toBuilder();

}
