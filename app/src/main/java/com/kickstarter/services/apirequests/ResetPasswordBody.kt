package com.kickstarter.services.apirequests;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class ResetPasswordBody implements Parcelable {
  public abstract String email();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder email(String __);
    public abstract ResetPasswordBody build();
  }

  public static Builder builder() {
    return new AutoParcel_ResetPasswordBody.Builder();
  }

  public abstract Builder toBuilder();
}
