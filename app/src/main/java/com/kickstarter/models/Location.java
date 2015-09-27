package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.kickstarter.libs.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson @AutoParcel
public abstract class Location implements Parcelable {
  public abstract String displayableName();
  public abstract long id();
  public abstract String name();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder displayableName(String __);
    public abstract Builder id(long __);
    public abstract Builder name(String __);
    public abstract Location build();
  }

  public static Builder builder() {
    return new AutoParcel_Location.Builder();
  }

  public abstract Builder toBuilder();
}
