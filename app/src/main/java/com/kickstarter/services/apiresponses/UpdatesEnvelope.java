package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.Update;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class UpdatesEnvelope implements Parcelable {
  public abstract List<Update> updates();
  public abstract UrlsEnvelope urls();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder updates(List<Update> __);
    public abstract Builder urls(UrlsEnvelope __);
    public abstract UpdatesEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_UpdatesEnvelope.Builder();
  }

  public abstract Builder toBuilder();

  @AutoGson
  @AutoParcel
  public abstract static class UrlsEnvelope implements Parcelable {
    public abstract ApiEnvelope api();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder api(ApiEnvelope __);
      public abstract UrlsEnvelope build();
    }

    public static Builder builder() {
      return new AutoParcel_UpdatesEnvelope_UrlsEnvelope.Builder();
    }

    public abstract Builder toBuilder();

    @AutoGson
    @AutoParcel
    public abstract static class ApiEnvelope implements Parcelable {
      public abstract String moreUpdates();

      @AutoParcel.Builder
      public abstract static class Builder {
        public abstract Builder moreUpdates(String __);
        public abstract ApiEnvelope build();
      }

      public static Builder builder() {
        return new AutoParcel_UpdatesEnvelope_UrlsEnvelope_ApiEnvelope.Builder();
      }
    }
  }
}
