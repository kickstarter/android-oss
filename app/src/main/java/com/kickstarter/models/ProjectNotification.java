package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoParcel
@AutoGson
public abstract class ProjectNotification implements Parcelable {
  public abstract Project project();
  public abstract long id();
  public abstract boolean email();
  public abstract boolean mobile();
  public abstract Urls urls();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder project(Project __);
    public abstract Builder id(long __);
    public abstract Builder email(boolean __);
    public abstract Builder mobile(boolean __);
    public abstract Builder urls(Urls __);
    public abstract ProjectNotification build();
  }

  public static Builder builder() {
    return new AutoParcel_ProjectNotification.Builder();
  }

  public abstract Builder toBuilder();

  @AutoParcel
  @AutoGson
  public abstract static class Project implements Parcelable {
    public abstract String name();
    public abstract long id();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder name(String __);
      public abstract Builder id(long __);
      public abstract Project build();
    }

    public static Builder builder() {
      return new AutoParcel_ProjectNotification_Project.Builder();
    }
  }

  @AutoParcel
  @AutoGson
  public abstract static class Urls implements Parcelable {
    public abstract Api api();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder api(Api __);
      public abstract Urls build();
    }

    public static Builder builder() {
      return new AutoParcel_ProjectNotification_Urls.Builder();
    }

    @AutoParcel
    @AutoGson
    public abstract static class Api implements Parcelable {
      public abstract String notification();

      @AutoParcel.Builder
      public abstract static class Builder {
        public abstract Builder notification(String __);
        public abstract Api build();
      }

      public static Builder builder() {
        return new AutoParcel_ProjectNotification_Urls_Api.Builder();
      }
    }
  }
}
