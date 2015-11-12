package com.kickstarter.services.apiresponses;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.Activity;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class ActivityEnvelope implements Parcelable {
  public abstract List<Activity> activities();
  @Nullable public abstract UrlsEnvelope urls();

  @AutoGson
  @AutoParcel
  public abstract static class UrlsEnvelope implements Parcelable {
    @Nullable public abstract ApiEnvelope api();

    @AutoGson
    @AutoParcel
    public abstract static class ApiEnvelope implements Parcelable {
      @Nullable public abstract String moreActivities();
      @Nullable public abstract String newerActivities();
    }
  }
}
