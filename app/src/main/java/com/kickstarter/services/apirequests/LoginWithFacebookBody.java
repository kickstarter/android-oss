package com.kickstarter.services.apirequests;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class LoginWithFacebookBody implements Parcelable {
  public abstract String accessToken();
  @Nullable public abstract String code();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder accessToken(String __);
    public abstract Builder code(String __);
    public abstract LoginWithFacebookBody build();
  }

  public static Builder builder() {
    return new AutoParcel_LoginWithFacebookBody.Builder();
  }

  public abstract Builder toBuilder();
}
