package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import org.joda.time.DateTime;

import java.util.List;

import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

import static com.kickstarter.libs.utils.IntegerUtils.isZero;

@AutoGson
@AutoParcel
public abstract class Reward implements Parcelable {
  public abstract @Nullable Integer backersCount();
  public abstract @Nullable String description();
  public abstract @Nullable DateTime endsAt();
  public abstract long id();
  public abstract @Nullable Integer limit();
  public abstract double minimum();
  public abstract @Nullable DateTime estimatedDeliveryOn();
  public abstract @Nullable Integer remaining();
  public abstract @Nullable List<RewardsItem> rewardsItems();
  public abstract @Nullable Boolean shippingEnabled();
  public abstract @Nullable String shippingPreference();
  public abstract @Nullable String shippingSummary();
  public abstract @Nullable String title();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder backersCount(Integer __);
    public abstract Builder description(String __);
    public abstract Builder endsAt(DateTime __);
    public abstract Builder id(long __);
    public abstract Builder limit(Integer __);
    public abstract Builder minimum(double __);
    public abstract Builder estimatedDeliveryOn(DateTime __);
    public abstract Builder remaining(Integer __);
    public abstract Builder rewardsItems(List<RewardsItem> __);
    public abstract Builder shippingEnabled(Boolean __);
    public abstract Builder shippingPreference(String __);
    public abstract Builder shippingSummary(String __);
    public abstract Builder title(String __);
    public abstract Reward build();
  }

  public static Builder builder() {
    return new AutoParcel_Reward.Builder();
  }

  public abstract Builder toBuilder();

  public boolean isAllGone() {
    return isZero(this.remaining());
  }

  public boolean isLimited() {
    return this.limit() != null && !this.isAllGone();
  }

}
