package com.kickstarter.services.apirequests;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class ProjectNotificationBody implements Parcelable {
  public abstract boolean email();
  public abstract boolean mobile();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder email(boolean __);
    public abstract Builder mobile(boolean __);
    public abstract ProjectNotificationBody build();
  }

  public static Builder builder() {
    return new AutoParcel_ProjectNotificationBody.Builder();
  }

  public abstract Builder toBuilder();
}
