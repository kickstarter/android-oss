package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import org.joda.time.DateTime;

import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class SurveyResponse implements Parcelable {
  public abstract @Nullable DateTime answeredAt();
  public abstract long id();
  public abstract @Nullable Project project();
  public abstract Urls urls();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder answeredAt(DateTime __);
    public abstract Builder id(long __);
    public abstract Builder project(Project __);
    public abstract Builder urls(Urls __);
    public abstract SurveyResponse build();
  }

  @AutoGson
  @AutoParcel
  public abstract static class Urls implements Parcelable {
    public abstract Web web();

    @AutoGson
    @AutoParcel
    public abstract static class Web implements Parcelable {
      public abstract String survey();

      @AutoParcel.Builder
      public abstract static class Builder {
        public abstract SurveyResponse.Urls.Web.Builder survey(String __);
        public abstract SurveyResponse.Urls.Web build();
      }

      public static Builder builder() {
        return new AutoParcel_SurveyResponse_Urls_Web.Builder();
      }
    }

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract SurveyResponse.Urls.Builder web(SurveyResponse.Urls.Web __);
      public abstract SurveyResponse.Urls build();
    }

    public static SurveyResponse.Urls.Builder builder() {
      return new AutoParcel_SurveyResponse_Urls.Builder();
    }
  }

  public static Builder builder() {
    return new AutoParcel_SurveyResponse.Builder();
  }

  public abstract Builder toBuilder();
}
