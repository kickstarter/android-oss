package com.kickstarter.services.apirequests;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class PushTokenBody implements Parcelable {
  public abstract String pushServer();
  public abstract String token();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder pushServer(String __);
    public abstract Builder token(String __);
    public abstract PushTokenBody build();
  }

  public static Builder builder() {
    return new AutoParcel_PushTokenBody.Builder();
  }

  public abstract Builder toBuilder();
}

