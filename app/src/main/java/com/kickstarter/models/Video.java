package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson @AutoParcel
public abstract class Video implements Parcelable {
  public abstract String base();
  public abstract String frame();
  public abstract String high();
  public abstract String webm();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder base(String __);
    public abstract Builder frame(String __);
    public abstract Builder high(String __);
    public abstract Builder webm(String __);
    public abstract Video build();
  }

  public static Builder builder() {
    return new AutoParcel_Video.Builder();
  }

  public abstract Builder toBuilder();
}
