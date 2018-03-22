package com.kickstarter.models;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Video implements Parcelable {
  public abstract String base();
  public abstract String frame();
  public abstract String high();
  public abstract @Nullable String hls();
  public abstract @Nullable String webm();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder base(String __);
    public abstract Builder frame(String __);
    public abstract Builder high(String __);
    public abstract Builder hls(String __);
    public abstract Builder webm(String __);
    public abstract Video build();
  }

  public static Builder builder() {
    return new AutoParcel_Video.Builder();
  }

  public abstract Builder toBuilder();
}
