package com.kickstarter.models;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Reward implements Parcelable {
  public abstract @Nullable Integer backersCount();
  public abstract @Nullable String description();
  public abstract long id();
  public abstract @Nullable Integer limit();
  public abstract float minimum();
  public abstract @Nullable DateTime estimatedDeliveryOn();
  public abstract @Nullable Integer remaining();
  public abstract String reward();
  public abstract @Nullable Boolean shippingEnabled();
  public abstract @Nullable String shippingPreference();
  public abstract @Nullable String shippingSummary();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder backersCount(Integer __);
    public abstract Builder description(String __);
    public abstract Builder id(long __);
    public abstract Builder limit(Integer __);
    public abstract Builder minimum(float __);
    public abstract Builder estimatedDeliveryOn(DateTime __);
    public abstract Builder remaining(Integer __);
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

  public boolean hasEstimatedDelivery() {
    return this.estimatedDeliveryOn() != null;
  }

  public boolean isAllGone() {
    return this.remaining() != null && this.remaining() == 0;
  }

  public boolean isLimited() {
    return this.limit() != null && !this.isAllGone();
  }

  public boolean isReward() {
    return this.id() != 0;
  }

  public boolean isNoReward() {
    return this.id() == 0;
  }
}
