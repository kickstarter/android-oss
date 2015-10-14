package com.kickstarter.services.apirequests;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class CommentBody implements Parcelable {
  public abstract String body();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder body(String __);
    public abstract CommentBody build();
  }

  public static Builder builder() {
    return new AutoParcel_CommentBody.Builder();
  }

  public abstract Builder toBuilder();
}
