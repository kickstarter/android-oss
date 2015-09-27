package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson @AutoParcel
public abstract class Photo implements Parcelable {
  public abstract String ed();
  public abstract String full();
  public abstract String little();
  public abstract String med();
  public abstract String small();
  public abstract String thumb();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder ed(String __);
    public abstract Builder full(String __);
    public abstract Builder little(String __);
    public abstract Builder med(String __);
    public abstract Builder small(String __);
    public abstract Builder thumb(String __);
    public abstract Photo build();
  }

  public static Builder builder() {
    return new AutoParcel_Photo.Builder();
  }

  public abstract Builder toBuilder();
}
