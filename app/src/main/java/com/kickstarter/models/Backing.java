package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import org.joda.time.DateTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Backing implements Parcelable {
  public abstract float amount();
  public abstract @Nullable User backer();
  public abstract @Nullable String backerNote();
  public abstract long backerId();
  public abstract @Nullable DateTime backerCompletedAt();
  public abstract @Nullable DateTime completedAt();
  public abstract long id();
  public abstract @Nullable Location location();
  public abstract DateTime pledgedAt();
  public abstract @Nullable Project project();
  public abstract String projectCountry();
  public abstract long projectId();
  public abstract @Nullable Reward reward();
  public abstract @Nullable Long rewardId();
  public abstract long sequence();
  public abstract float shippingAmount();
  public abstract @Status String status();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder amount(float __);
    public abstract Builder backer(User __);
    public abstract Builder backerNote(String __);
    public abstract Builder backerId(long __);
    public abstract Builder backerCompletedAt(DateTime __);
    public abstract Builder completedAt(DateTime __);
    public abstract Builder id(long __);
    public abstract Builder location(Location __);
    public abstract Builder pledgedAt(DateTime __);
    public abstract Builder project(Project __);
    public abstract Builder projectCountry(String __);
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
}
