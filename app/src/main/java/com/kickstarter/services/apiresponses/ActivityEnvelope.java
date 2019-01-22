package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.Activity;

import java.util.List;

import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class ActivityEnvelope implements Parcelable {
  public abstract List<Activity> activities();
  public abstract UrlsEnvelope urls();

  @AutoGson
  @AutoParcel
  public abstract static class UrlsEnvelope implements Parcelable {
    public abstract ApiEnvelope api();

    @AutoGson
    @AutoParcel
    public abstract static class ApiEnvelope implements Parcelable {
      public abstract String moreActivities();
      public abstract @Nullable String newerActivities();

      @AutoParcel.Builder
      public abstract static class Builder {
        public abstract Builder moreActivities(String __);
        public abstract Builder newerActivities(String __);
        public abstract ApiEnvelope build();
      }

      public static Builder builder() {
        return new AutoParcel_ActivityEnvelope_UrlsEnvelope_ApiEnvelope.Builder();
      }
    }

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder api(ApiEnvelope __);
      public abstract UrlsEnvelope build();
    }

    public static Builder builder() {
      return new AutoParcel_ActivityEnvelope_UrlsEnvelope.Builder();
    }
  }

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder activities(List<Activity> __);
    public abstract Builder urls(UrlsEnvelope __);
    public abstract ActivityEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_ActivityEnvelope.Builder();
  }

  public abstract Builder toBuilder();
}
