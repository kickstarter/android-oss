package com.kickstarter.services.apirequests;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class RegisterWithFacebookBody implements Parcelable {
  public abstract String accessToken();
  public abstract boolean sendNewsletters();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder accessToken(String __);
    public abstract Builder sendNewsletters(boolean __);
    public abstract RegisterWithFacebookBody build();
  }

  public static Builder builder() {
    return new AutoParcel_RegisterWithFacebookBody.Builder();
  }

  public abstract Builder toBuilder();

}
