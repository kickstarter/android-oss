package com.kickstarter.libs;

import android.os.Parcelable;

import org.joda.time.DateTime;

import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

@AutoParcel
public abstract class RelativeDateTimeOptions implements Parcelable {
  /**
   * Abbreviates string, e.g.: "in 1 hr"
   */
  public abstract boolean abbreviated();

  /**
   * Don't output tense, e.g.: "1 hour" instead of "in 1 hour"
   */
  public abstract boolean absolute();

  /**
   * Compare against this date instead of the current time
   */
  public abstract @Nullable DateTime relativeToDateTime();

  /**
   * Number of seconds difference permitted before an attempt to describe the relative date is abandoned.
   * For example, "738 days ago" is not helpful to users. The threshold defaults to 30 days.
   */
  public abstract int threshold();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder abbreviated(boolean __);
    public abstract Builder absolute(boolean __);
    public abstract Builder relativeToDateTime(DateTime __);
    public abstract Builder threshold(int __);
    public abstract RelativeDateTimeOptions build();
  }

  public static Builder builder() {
    return new AutoParcel_RelativeDateTimeOptions.Builder()
      .abbreviated(false)
      .absolute(false)
      .threshold(THIRTY_DAYS_IN_SECONDS);
  }

  public abstract Builder toBuilder();

  private final static int THIRTY_DAYS_IN_SECONDS = 60 * 60 * 24 * 30;
}


