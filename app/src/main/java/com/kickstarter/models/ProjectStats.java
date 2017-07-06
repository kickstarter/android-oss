package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import org.joda.time.DateTime;

import java.util.List;

import auto.parcel.AutoParcel;
import rx.schedulers.TimeInterval;

import static com.kickstarter.R.id.pledged;

@AutoGson
@AutoParcel
public abstract class ProjectStats implements Parcelable {

  public enum ReferrerType {
    CUSTOM, EXTERNAL, INTERNAL, UNKNOWN
  }

  public abstract CumulativeStats cumulativeStats();
  public abstract FundingDateStats fundingDateStats();
  public abstract ReferrerStats referrerStats();
  public abstract List<RewardStats> rewardDistribution();
  public abstract VideoStats videoStats();


  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder cumulativeStats(CumulativeStats __);
    public abstract Builder fundingDateStats(FundingDateStats __);
    public abstract Builder referrerStats(ReferrerStats __);
    public abstract Builder rewardDistribution(List<RewardStats> __);
    public abstract Builder videoStats(VideoStats __);
    public abstract ProjectStats build();
  }

  public static Builder builder() { return new AutoParcel_ProjectStats.Builder(); }

  public abstract Builder toBuilder();

  @AutoParcel
  @AutoGson
  public abstract static class CumulativeStats implements Parcelable {
    public abstract int averagePledge();
    public abstract int backersCount();
    public abstract int goal();
    public abstract double percentRaised();
    public abstract int pledged();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder averagePledge(int __);
      public abstract Builder backersCount(int __);
      public abstract Builder goal(int __);
      public abstract Builder percentRaised(double __);
      public abstract Builder pledged(int __);
      public abstract CumulativeStats build();
    }

    public static Builder builder() { return new AutoParcel_ProjectStats_CumulativeStats.Builder(); }

    public abstract Builder toBuilder();
  }

  @AutoParcel
  @AutoGson
  public abstract static class FundingDateStats {
    public abstract int backersCount();
    public abstract int cumulativePledged();
    public abstract int cumulativeBackersCount();
    public abstract DateTime timeInterval();
    public abstract int pledged();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder backersCount(int __);
      public abstract Builder cumulativePledged(int __);
      public abstract Builder cumulativeBackersCount(int __);
      public abstract Builder timeInterval(DateTime __);
      public abstract Builder pledged(int __);
      public abstract FundingDateStats build();
    }

    public static Builder builder() { return new AutoParcel_ProjectStats_FundingDateStats.Builder(); }

    public abstract Builder toBuilder();
  }

  @AutoParcel
  @AutoGson
  public abstract static class ReferrerStats {
    public abstract int backersCount();
    public abstract String code();
    public abstract double percentageOfDollars();
    public abstract int pledged();
    public abstract String referrerName();
    public abstract ReferrerType referrerType();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder backersCount(int __);
      public abstract Builder code(String __);
      public abstract Builder percentageOfDollars(double __);
      public abstract Builder pledged(int __);
      public abstract Builder referrerName(String __);
      public abstract Builder referrerType(ReferrerType __);
      public abstract ReferrerStats build();
    }

    public static Builder builder() { return new AutoParcel_ProjectStats_ReferrerStats.Builder(); }

    public abstract Builder toBuilder();
  }

  @AutoParcel
  @AutoGson
  public abstract static class RewardStats {
    public abstract int backersCount();
    public abstract int rewardId();
    public abstract int minimum();
    public abstract int pledged();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder backersCount(int __);
      public abstract Builder rewardId(int __);
      public abstract Builder minimum(int __);
      public abstract Builder pledged(int __);
      public abstract RewardStats build();
    }

    public static Builder builder() { return new AutoParcel_ProjectStats_RewardStats.Builder(); }

    public abstract Builder toBuilder();
//    public static RewardStats zero = RewardStats(backersCount: 0, rewardId: 0, minimum: 0, pledged: 0)
  }

  @AutoParcel
  @AutoGson
  public abstract static class VideoStats {
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

    public static Builder builder() { return new AutoParcel_ProjectStats_VideoStats.Builder(); }

    public abstract Builder toBuilder();
  }
}
