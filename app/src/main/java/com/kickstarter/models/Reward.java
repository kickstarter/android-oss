package com.kickstarter.models;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.AutoGson;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Reward implements Parcelable {
  @Nullable public abstract Integer backersCount();
  @Nullable public abstract String description();
  public abstract long id();
  @Nullable public abstract Integer limit();
  public abstract float minimum();
  @Nullable public abstract DateTime estimatedDeliveryOn();
  public abstract String reward();
  @Nullable public abstract Boolean shippingEnabled();
  @Nullable public abstract String shippingPreference();
  @Nullable public abstract String shippingSummary();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder backersCount(Integer __);
    public abstract Builder description(String __);
    public abstract Builder id(long __);
    public abstract Builder limit(Integer __);
    public abstract Builder minimum(float __);
    public abstract Builder estimatedDeliveryOn(DateTime __);
    public abstract Builder reward(String __);
    public abstract Builder shippingEnabled(Boolean __);
    public abstract Builder shippingPreference(String __);
    public abstract Builder shippingSummary(String __);
    public abstract Reward build();
  }

  public static Builder builder() {
    return new AutoParcel_Reward.Builder();
  }

  public abstract Builder toBuilder();

  public boolean isReward() {
    return this.id() != 0;
  }

  public boolean isNoReward() {
    return this.id() == 0;
  }
}
