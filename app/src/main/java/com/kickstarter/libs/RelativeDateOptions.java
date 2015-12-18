package com.kickstarter.libs;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class RelativeDateOptions implements Parcelable {
  public abstract boolean abbreviated();
  public abstract boolean absolute();
  public abstract @Nullable DateTime relativeToDateTime();
  public abstract int threshold();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder abbreviated(boolean __);
    public abstract Builder absolute(boolean __);
    public abstract Builder relativeToDateTime(DateTime __);
    public abstract Builder threshold(int __);
    public abstract RelativeDateOptions build();
  }

  public static Builder builder() {
    return new AutoParcel_RelativeDateOptions.Builder()
      .abbreviated(false)
      .absolute(false)
      .threshold(THIRTY_DAYS_IN_SECONDS);
  }

  public abstract Builder toBuilder();

  private final static int THIRTY_DAYS_IN_SECONDS = 60 * 60 * 24 * 30;
}


