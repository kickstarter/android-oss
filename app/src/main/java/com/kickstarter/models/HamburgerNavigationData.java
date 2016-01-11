package com.kickstarter.models;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.services.DiscoveryParams;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class HamburgerNavigationData implements Parcelable {
  public abstract @NonNull List<DiscoveryParams> categoryFilters();
  public abstract @Nullable User user();
  public abstract @NonNull List<DiscoveryParams> topFilters();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder categoryFilters(List<DiscoveryParams> __);
    public abstract Builder user(User __);
    public abstract Builder topFilters(List<DiscoveryParams> __);
    public abstract HamburgerNavigationData build();
  }

  public static Builder builder() {
    return new AutoParcel_HamburgerNavigationData.Builder();
  }
}
