package com.kickstarter.models;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class SurveyResponse implements Parcelable {
  public abstract @Nullable DateTime answeredAt();
  public abstract int id();
  public abstract @Nullable Project project();
  public abstract UrlsEnvelope urls();

  @AutoGson
  @AutoParcel
  public abstract static class UrlsEnvelope implements Parcelable {
    public abstract WebEnvelope web();

    @AutoGson
    @AutoParcel
    public abstract static class WebEnvelope implements Parcelable {
      public abstract String survey();

      @AutoParcel.Builder
      public abstract static class Builder {
        public abstract SurveyResponse.UrlsEnvelope.WebEnvelope.Builder survey(String __);
        public abstract SurveyResponse.UrlsEnvelope.WebEnvelope build();
      }

      public static Builder builder() {
        return new AutoParcel_SurveyResponse_UrlsEnvelope_WebEnvelope.Builder();
      }
    }

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract SurveyResponse.UrlsEnvelope.Builder web(SurveyResponse.UrlsEnvelope.WebEnvelope __);
      public abstract SurveyResponse.UrlsEnvelope build();
    }

    public static SurveyResponse.UrlsEnvelope.Builder builder() {
      return new AutoParcel_SurveyResponse_UrlsEnvelope.Builder();
    }
  }

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder answeredAt(DateTime __);
    public abstract Builder id(int __);
    public abstract Builder project(Project __);
    public abstract Builder urls(UrlsEnvelope __);
    public abstract SurveyResponse build();
  }

  public static Builder builder() {
    return new AutoParcel_SurveyResponse.Builder();
  }

  public abstract Builder toBuilder();
}
