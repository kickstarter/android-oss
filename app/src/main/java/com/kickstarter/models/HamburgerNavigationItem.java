package com.kickstarter.models;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.kickstarter.services.DiscoveryParams;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class HamburgerNavigationItem implements Parcelable {
  public abstract @NonNull DiscoveryParams discoveryParams();
  public abstract boolean selected();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder discoveryParams(DiscoveryParams __);
    public abstract Builder selected(boolean __);
    public abstract HamburgerNavigationItem build();
  }

  public static Builder builder() {
    return new AutoParcel_HamburgerNavigationItem.Builder().selected(false);
  }
}
