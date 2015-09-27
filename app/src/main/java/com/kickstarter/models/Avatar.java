package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson @AutoParcel
public abstract class Avatar implements Parcelable {
  public abstract String medium();
  public abstract String small();
  public abstract String thumb();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder medium(String __);
    public abstract Builder small(String __);
    public abstract Builder thumb(String __);
    public abstract Avatar build();
  }

  public static Builder builder() {
    return new AutoParcel_Avatar.Builder();
  }
}
