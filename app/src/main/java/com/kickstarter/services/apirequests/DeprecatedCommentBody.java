package com.kickstarter.services.apirequests;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class DeprecatedCommentBody implements Parcelable {
  public abstract String body();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder body(String __);
    public abstract DeprecatedCommentBody build();
  }

  public static Builder builder() {
    return new AutoParcel_DeprecatedCommentBody.Builder();
  }

  public abstract Builder toBuilder();
}
