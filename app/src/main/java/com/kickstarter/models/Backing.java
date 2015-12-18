package com.kickstarter.models;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Backing implements Parcelable {
  @Nullable public abstract DateTime completedAt();
  public abstract String projectCountry();
  @Nullable public abstract Reward reward();
  public abstract String status();
  public abstract float shippingAmount();
  public abstract float amount();
  @Nullable public abstract Project project();
  public abstract long backerId();
  @Nullable public abstract Location location();
  public abstract DateTime pledgedAt();
  @Nullable public abstract Long rewardId();
  public abstract long id();
  public abstract long projectId();
  public abstract long sequence(); // backer #
  @Nullable public abstract User backer();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder completedAt(DateTime __);
    public abstract Builder projectCountry(String __);
    public abstract Builder reward(Reward __);
    public abstract Builder status(String __);
    public abstract Builder shippingAmount(float __);
    public abstract Builder amount(float __);
    public abstract Builder project(Project __);
    public abstract Builder backerId(long __);
    public abstract Builder location(Location __);
    public abstract Builder pledgedAt(DateTime __);
    public abstract Builder rewardId(Long __);
    public abstract Builder id(long __);
    public abstract Builder projectId(long __);
    public abstract Builder sequence(long __);
    public abstract Builder backer(User __);
    public abstract Backing build();
  }

  public static Builder builder() {
    return new AutoParcel_Backing.Builder();
  }

  public abstract Builder toBuilder();

  public String backerNumber() {
    return String.valueOf(this.sequence());
  }

  public String formattedSequence() {
    return NumberUtils.numberWithDelimiter((int) this.sequence());
  }
}
