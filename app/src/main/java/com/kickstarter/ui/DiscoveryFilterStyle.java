package com.kickstarter.ui;

import android.os.Parcelable;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class DiscoveryFilterStyle implements Parcelable {
  public abstract boolean primary();
  public abstract boolean selected();
  public abstract boolean visible();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder primary(boolean __);
    public abstract Builder selected(boolean __);
    public abstract Builder visible(boolean __);
    public abstract DiscoveryFilterStyle build();
  }

  public static Builder builder() {
    return new AutoParcel_DiscoveryFilterStyle.Builder();
  }

  public abstract Builder toBuilder();
}
