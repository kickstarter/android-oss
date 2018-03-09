package com.kickstarter.services.apiresponses;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.kickstarter.libs.ReferrerType;
import com.kickstarter.libs.qualifiers.AutoGson;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Locale;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class ProjectStatsEnvelope implements Parcelable {
  public abstract CumulativeStats cumulative();
  public abstract List<FundingDateStats> fundingDistribution();
  public abstract List<ReferrerStats> referralDistribution();
  public abstract List<RewardStats> rewardDistribution();
  public abstract VideoStats videoStats();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder cumulative(CumulativeStats __);
    public abstract Builder fundingDistribution(List<FundingDateStats> __);
    public abstract Builder referralDistribution(List<ReferrerStats> __);
    public abstract Builder rewardDistribution(List<RewardStats> __);
    public abstract Builder videoStats(VideoStats __);
    public abstract ProjectStatsEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_ProjectStatsEnvelope.Builder();
  }

  public abstract Builder toBuilder();

  @AutoParcel
  @AutoGson
  public abstract static class CumulativeStats implements Parcelable {
    public abstract float averagePledge();
    public abstract int backersCount();
    public abstract int goal();
    public abstract float percentRaised();
    public abstract float pledged();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder averagePledge(float __);
      public abstract Builder backersCount(int __);
      public abstract Builder goal(int __);
      public abstract Builder percentRaised(float __);
      public abstract Builder pledged(float __);
      public abstract CumulativeStats build();
    }

    public static Builder builder() {
      return new AutoParcel_ProjectStatsEnvelope_CumulativeStats.Builder();
    }

    public abstract Builder toBuilder();
  }

  @AutoParcel
  @AutoGson
  public abstract static class FundingDateStats implements Parcelable {
    public abstract int backersCount();
    public abstract float cumulativePledged();
    public abstract int cumulativeBackersCount();
    public abstract DateTime date();
    public abstract float pledged();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder backersCount(int __);
      public abstract Builder cumulativePledged(float __);
      public abstract Builder cumulativeBackersCount(int __);
      public abstract Builder date(DateTime __);
      public abstract Builder pledged(float __);
      public abstract FundingDateStats build();
    }

    public static Builder builder() {
      return new AutoParcel_ProjectStatsEnvelope_FundingDateStats.Builder();
    }

    public abstract Builder toBuilder();
  }

  @AutoParcel
  @AutoGson
  public abstract static class ReferrerStats implements Parcelable {
    public abstract int backersCount();
    public abstract String code();
    public abstract float percentageOfDollars();
    public abstract float pledged();
    public abstract String referrerName();
    public abstract String referrerType();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder backersCount(int __);
      public abstract Builder code(String __);
      public abstract Builder percentageOfDollars(float __);
      public abstract Builder pledged(float __);
      public abstract Builder referrerName(String __);
      public abstract Builder referrerType(String __);
      public abstract ReferrerStats build();
    }

    // Deserialize the referrer type string names into the corresponding
    // enum type.
    public static ReferrerType referrerTypeEnum(final @NonNull String referrerType) {
      switch (referrerType.toLowerCase(Locale.getDefault())) {
        case "custom":
          return ReferrerType.CUSTOM;
        case "external":
          return ReferrerType.EXTERNAL;
        case "kickstarter":
          return ReferrerType.KICKSTARTER;
        default:
          return ReferrerType.KICKSTARTER;
      }
    }

    public static Builder builder() {
      return new AutoParcel_ProjectStatsEnvelope_ReferrerStats.Builder();
    }

    public abstract Builder toBuilder();
  }

  @AutoParcel
  @AutoGson
  public abstract static class RewardStats implements Parcelable {
    public abstract int backersCount();
    public abstract int rewardId();
    public abstract int minimum();
    public abstract float pledged();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder backersCount(int __);
      public abstract Builder rewardId(int __);
      public abstract Builder minimum(int __);
      public abstract Builder pledged(float __);
      public abstract RewardStats build();
    }

    public static Builder builder() {
      return new AutoParcel_ProjectStatsEnvelope_RewardStats.Builder();
    }

    public abstract Builder toBuilder();
  }

  @AutoParcel
  @AutoGson
  public abstract static class VideoStats implements Parcelable {
    public abstract int externalCompletions();
    public abstract int externalStarts();
    public abstract int internalCompletions();
    public abstract int internalStarts();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder externalCompletions(int __);
      public abstract Builder externalStarts(int __);
      public abstract Builder internalCompletions(int __);
      public abstract Builder internalStarts(int __);
      public abstract VideoStats build();
    }

    public static Builder builder() {
      return new AutoParcel_ProjectStatsEnvelope_VideoStats.Builder();
    }

    public abstract Builder toBuilder();
  }
}
