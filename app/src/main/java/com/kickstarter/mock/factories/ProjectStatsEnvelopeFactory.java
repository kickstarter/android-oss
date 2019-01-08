package com.kickstarter.mock.factories;

import com.kickstarter.libs.ReferrerType;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

public final class ProjectStatsEnvelopeFactory {
  private ProjectStatsEnvelopeFactory() {}

  public static @NonNull ProjectStatsEnvelope projectStatsEnvelope() {
    final ProjectStatsEnvelope.CumulativeStats cumulativeStats = CumulativeStatsFactory
      .cumulativeStats()
      .toBuilder()
      .build();

    final ProjectStatsEnvelope.FundingDateStats fundingDateStats = FundingDateStatsFactory
      .fundingDateStats()
      .toBuilder()
      .build();

    final ProjectStatsEnvelope.ReferrerStats referrerStats = ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .build();

    final ProjectStatsEnvelope.ReferralAggregateStats referralAggregates = ReferralAggregateStatsFactory
      .referralAggregates()
      .toBuilder()
      .build();

    final ProjectStatsEnvelope.RewardStats rewardStats = RewardStatsFactory
      .rewardStats()
      .toBuilder()
      .build();

    final ProjectStatsEnvelope.VideoStats videoStats = VideoStatsFactory
      .videoStats()
      .toBuilder()
      .build();

    final List<ProjectStatsEnvelope.FundingDateStats> fundingDateStatsList = Collections.singletonList(fundingDateStats);
    final List<ProjectStatsEnvelope.RewardStats> rewardStatsList = Collections.singletonList(rewardStats);
    final List<ProjectStatsEnvelope.ReferrerStats> referrerStatsList = Collections.singletonList(referrerStats);

    return ProjectStatsEnvelope.builder()
      .cumulative(cumulativeStats)
      .fundingDistribution(fundingDateStatsList)
      .referralAggregates(referralAggregates)
      .referralDistribution(referrerStatsList)
      .rewardDistribution(rewardStatsList)
      .videoStats(videoStats)
      .build();
  }

  public static final class CumulativeStatsFactory {
    private CumulativeStatsFactory() {}

    public static @NonNull ProjectStatsEnvelope.CumulativeStats cumulativeStats() {
      return ProjectStatsEnvelope.CumulativeStats.builder()
        .averagePledge(5)
        .backersCount(10)
        .goal(1000)
        .percentRaised(50)
        .pledged(500)
        .build();
    }
  }

  public static final class FundingDateStatsFactory {
    private FundingDateStatsFactory() {}

    public static @NonNull ProjectStatsEnvelope.FundingDateStats fundingDateStats() {
      return ProjectStatsEnvelope.FundingDateStats.builder()
        .backersCount(10)
        .cumulativePledged(500)
        .cumulativeBackersCount(10)
        .date(new DateTime())
        .pledged(500)
        .build();
    }
  }

  public static final class ReferralAggregateStatsFactory {
    private ReferralAggregateStatsFactory() {}

    public static @NonNull ProjectStatsEnvelope.ReferralAggregateStats referralAggregates() {
      return ProjectStatsEnvelope.ReferralAggregateStats.builder()
        .custom(10)
        .external(15)
        .internal(20)
        .build();
    }
  }

  public static final class ReferrerStatsFactory {
    private ReferrerStatsFactory() {}

    public static @NonNull ProjectStatsEnvelope.ReferrerStats referrerStats() {
      return ProjectStatsEnvelope.ReferrerStats.builder()
        .backersCount(10)
        .code("wots_this")
        .percentageOfDollars(50)
        .pledged(500)
        .referrerName("Important Referrer")
        .referrerType(ReferrerType.EXTERNAL.getReferrerType())
        .build();
    }
  }

  public static final class RewardStatsFactory {
    private RewardStatsFactory() {}

    public static @NonNull ProjectStatsEnvelope.RewardStats rewardStats() {
      return ProjectStatsEnvelope.RewardStats.builder()
        .backersCount(10)
        .rewardId(1)
        .minimum(5)
        .pledged(10)
        .build();
    }
  }

  public static final class VideoStatsFactory {
    private VideoStatsFactory() {}

    public static @NonNull ProjectStatsEnvelope.VideoStats videoStats() {
      return ProjectStatsEnvelope.VideoStats.builder()
        .externalCompletions(1000)
        .externalStarts(2000)
        .internalCompletions(500)
        .internalStarts(1000)
        .build();
    }
  }
}
