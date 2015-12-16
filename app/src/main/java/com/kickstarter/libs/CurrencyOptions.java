package com.kickstarter.libs;

import android.os.Parcelable;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class CurrencyOptions implements Parcelable {
  public abstract String country();
  public abstract String currencyCode();
  public abstract String currencySymbol();
  public abstract float value();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder country(String __);
    public abstract Builder currencyCode(String __);
    public abstract Builder currencySymbol(String __);
    public abstract Builder value(float __);
    public abstract CurrencyOptions build();
  }

  public static Builder builder() {
    return new AutoParcel_CurrencyOptions.Builder();
  }

  public abstract Builder toBuilder();
}
