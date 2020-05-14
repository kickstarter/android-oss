package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import org.joda.time.DateTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Backing implements Parcelable, Relay {
  public abstract double amount();
  public abstract @Nullable User backer();
  public abstract @Nullable String backerNote();
  public abstract long backerId();
  public abstract @Nullable String backerName();
  public abstract @Nullable String backerUrl();
  public abstract @Nullable DateTime backerCompletedAt();
  public abstract boolean cancelable();
  public abstract @Nullable DateTime completedAt();
  public abstract long id();
  public abstract @Nullable Location location();
  public abstract @Nullable Long locationId();
  public abstract @Nullable String locationName();
  public abstract @Nullable PaymentSource paymentSource();
  public abstract DateTime pledgedAt();
  public abstract @Nullable Project project();
  public abstract long projectId();
  public abstract @Nullable Reward reward();
  public abstract @Nullable Long rewardId();
  public abstract long sequence();
  public abstract float shippingAmount();
  public abstract @Status String status();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder amount(double __);
    public abstract Builder backer(User __);
    public abstract Builder backerNote(String __);
    public abstract Builder backerName(String __);
    public abstract Builder backerUrl(String __);
    public abstract Builder backerId(long __);
    public abstract Builder backerCompletedAt(DateTime __);
    public abstract Builder cancelable(boolean __);
    public abstract Builder completedAt(DateTime __);
    public abstract Builder id(long __);
    public abstract Builder location(Location __);
    public abstract Builder locationId(Long __);
    public abstract Builder locationName(String __);
    public abstract Builder paymentSource(PaymentSource __);
    public abstract Builder pledgedAt(DateTime __);
    public abstract Builder project(Project __);
    public abstract Builder projectId(long __);
    public abstract Builder reward(Reward __);
    public abstract Builder rewardId(Long __);
    public abstract Builder sequence(long __);
    public abstract Builder shippingAmount(float __);
    public abstract Builder status(@Status String __);
    public abstract Backing build();
  }

  public static Builder builder() {
    return new AutoParcel_Backing.Builder();
  }

  public abstract Builder toBuilder();

  public static final String STATUS_CANCELED      = "canceled";
  public static final String STATUS_COLLECTED     = "collected";
  public static final String STATUS_DROPPED       = "dropped";
  public static final String STATUS_ERRORED       = "errored";
  public static final String STATUS_PLEDGED       = "pledged";
  public static final String STATUS_PREAUTH       = "preauth";

  @Retention(RetentionPolicy.SOURCE)
  @StringDef({STATUS_CANCELED, STATUS_COLLECTED, STATUS_DROPPED, STATUS_ERRORED, STATUS_PLEDGED, STATUS_PREAUTH})
  public @interface Status {}

  @AutoParcel
  @AutoGson
  public abstract static class PaymentSource implements Parcelable {
    public abstract String id();
    public abstract String paymentType();
    public abstract String state();
    public abstract @Nullable String type();
    public abstract @Nullable String lastFour();
    public abstract @Nullable Date expirationDate();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder id(String __);
      public abstract Builder paymentType(String __);
      public abstract Builder state(String __);
      public abstract Builder type(String __);
      public abstract Builder lastFour(String __);
      public abstract Builder expirationDate(Date __);
      public abstract PaymentSource build();
    }

    public static PaymentSource.Builder builder() {
      return new AutoParcel_Backing_PaymentSource.Builder();
    }

    public abstract Builder toBuilder();
  }
}
