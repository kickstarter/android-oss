package com.kickstarter.libs;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class NumberOptions implements Parcelable {
  public abstract @Nullable Float bucketAbove();
  public abstract @Nullable Integer bucketPrecision();
  public abstract @Nullable String currencyCode();
  public abstract @Nullable String currencySymbol();
  public abstract @Nullable Integer precision();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder bucketAbove(Float __);
    public abstract Builder bucketPrecision(Integer __);
    public abstract Builder currencyCode(String __);
    public abstract Builder currencySymbol(String __);
    public abstract Builder precision(Integer __);
    public abstract NumberOptions build();
  }

  public static Builder builder() {
    return new AutoParcel_NumberOptions.Builder();
  }

  public abstract Builder toBuilder();

  public boolean isCurrency() {
    return currencyCode() != null || currencySymbol() != null;
  }
}
