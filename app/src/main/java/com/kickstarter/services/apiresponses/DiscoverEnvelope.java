package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.Project;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class DiscoverEnvelope implements Parcelable {
  public abstract List<Project> projects();
  public abstract UrlsEnvelope urls();

  @AutoGson
  @AutoParcel
  public abstract static class UrlsEnvelope implements Parcelable {
    public abstract ApiEnvelope api();

    @AutoGson
    @AutoParcel
    public abstract static class ApiEnvelope implements Parcelable {
      public abstract String moreProjects();

      @AutoParcel.Builder
      public abstract static class Builder {
        public abstract Builder moreProjects(String __);
        public abstract ApiEnvelope build();
      }

      public static Builder builder() {
        return new AutoParcel_DiscoverEnvelope_UrlsEnvelope_ApiEnvelope.Builder();
      }
    }

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder api(ApiEnvelope __);
      public abstract UrlsEnvelope build();
    }

    public static Builder builder() {
      return new AutoParcel_DiscoverEnvelope_UrlsEnvelope.Builder();
    }
  }

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder projects(List<Project> __);
    public abstract Builder urls(UrlsEnvelope __);
    public abstract DiscoverEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_DiscoverEnvelope.Builder();
  }

  public abstract Builder toBuilder();
}
