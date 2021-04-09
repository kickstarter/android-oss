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
  public abstract StatsEnvelope stats();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder projects(List<Project> __);
    public abstract Builder urls(UrlsEnvelope __);
    public abstract Builder stats(StatsEnvelope __);
    public abstract DiscoverEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_DiscoverEnvelope.Builder();
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
      return new AutoParcel_DiscoverEnvelope_UrlsEnvelope.Builder();
    }

    public abstract Builder toBuilder();

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

  }

  @AutoGson
  @AutoParcel
  public abstract static class StatsEnvelope implements Parcelable {
    public abstract Integer count();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder count(Integer __);
      public abstract StatsEnvelope build();
    }
    public static Builder builder() {
      return new AutoParcel_DiscoverEnvelope_StatsEnvelope.Builder();
    }
  }

}
