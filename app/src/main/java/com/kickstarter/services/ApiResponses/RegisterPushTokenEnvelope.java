package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoParcel
@AutoGson
public abstract class RegisterPushTokenEnvelope implements Parcelable {
  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract RegisterPushTokenEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_RegisterPushTokenEnvelope.Builder();
  }

  public abstract Builder toBuilder();
}
