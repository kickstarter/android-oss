package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import org.joda.time.DateTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import auto.parcel.AutoParcel;

import static com.kickstarter.libs.utils.IntegerUtils.isZero;

@AutoGson
@AutoParcel
public abstract class Reward implements Parcelable, Relay {
  public abstract @Nullable Integer backersCount();
  public abstract @Nullable double convertedMinimum();
  public abstract @Nullable String description();
  public abstract @Nullable DateTime endsAt();
  public abstract long id();
  public abstract @Nullable Integer limit();
  public abstract double minimum();
  public abstract @Nullable DateTime estimatedDeliveryOn();
  public abstract @Nullable Integer remaining();
  public abstract @Nullable List<RewardsItem> rewardsItems();
  public abstract @Nullable String shippingPreference();
  public abstract @Nullable SingleLocation shippingSingleLocation();
  public abstract @Nullable @ShippingType String shippingType();
  public abstract @Nullable String title();
  public abstract @Nullable boolean isAddOn();
  public abstract @Nullable List<RewardsItem> addOnsItems();
  public abstract @Nullable Integer quantity();
  public abstract @Nullable boolean hasAddons();

  /**
   * this field will be available just for GraphQL, in V1 it would be empty
   */
  public abstract @Nullable ShippingPreference shippingPreferenceType();

  /**
   * this field will be available just for GraphQL, in V1 it would be empty
   */
  public abstract @Nullable List<ShippingRule> shippingRules();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder backersCount(Integer __);
    public abstract Builder convertedMinimum(double __);
    public abstract Builder description(String __);
    public abstract Builder endsAt(DateTime __);
    public abstract Builder id(long __);
    public abstract Builder limit(Integer __);
    public abstract Builder minimum(double __);
    public abstract Builder estimatedDeliveryOn(DateTime __);
    public abstract Builder remaining(Integer __);
    public abstract Builder rewardsItems(List<RewardsItem> __);
    public abstract Builder shippingPreference(String __);
    public abstract Builder shippingSingleLocation(SingleLocation __);
    public abstract Builder shippingType(@ShippingType String __);
    public abstract Builder title(String __);
    public abstract Builder isAddOn(boolean __);
    public abstract Builder addOnsItems(List<RewardsItem> __);
    public abstract Builder quantity(Integer __);
    public abstract Builder hasAddons(boolean __);
    public abstract Builder shippingRules(List<ShippingRule> __);
    public abstract Builder shippingPreferenceType(ShippingPreference __);
    public abstract Reward build();
  }

  public static Builder builder() {
    return new AutoParcel_Reward.Builder();
  }

  public abstract Builder toBuilder();

  public static final String SHIPPING_TYPE_ANYWHERE = "anywhere";
  public static final String SHIPPING_TYPE_MULTIPLE_LOCATIONS = "multiple_locations";
  public static final String SHIPPING_TYPE_NO_SHIPPING = "no_shipping";
  public static final String SHIPPING_TYPE_SINGLE_LOCATION = "single_location";

  @Retention(RetentionPolicy.SOURCE)
  @StringDef({SHIPPING_TYPE_ANYWHERE, SHIPPING_TYPE_MULTIPLE_LOCATIONS, SHIPPING_TYPE_NO_SHIPPING, SHIPPING_TYPE_SINGLE_LOCATION})
  public @interface ShippingType {}

  public boolean isAllGone() {
    return isZero(this.remaining());
  }

  public boolean isLimited() {
    return this.limit() != null && !this.isAllGone();
  }

  public enum ShippingPreference {
    NONE("none"),

    RESTRICTED("restricted"),

    UNRESTRICTED("unrestricted"),

    UNKNOWN("$UNKNOWN");

    private String type;

    ShippingPreference(final String type) {
      this.type = type;
    }
  }

  @AutoParcel
  @AutoGson
  public abstract static class SingleLocation implements Parcelable {
    public abstract long id();
    public abstract String localizedName();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder id(long __);
      public abstract Builder localizedName(String __);
      public abstract SingleLocation build();
    }

    public static SingleLocation.Builder builder() {
      return new AutoParcel_Reward_SingleLocation.Builder();
    }

    public abstract Builder toBuilder();
  }
}
