package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.kickstarter.libs.AutoGson;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoGson @AutoParcel
public abstract class Comment implements Parcelable {
  public abstract User author();
  public abstract String body();
  public abstract DateTime createdAt();
  public abstract long id();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder author(User __);
    public abstract Builder body(String __);
    public abstract Builder createdAt(DateTime __);
    public abstract Builder id(long __);
    public abstract Comment build();
  }

  public static Builder builder() {
    return new AutoParcel_Comment.Builder();
  }

  public abstract Builder toBuilder();
}
