package com.kickstarter.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.ProjectStats;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ProjectStatsFactory {
  private ProjectStatsFactory() {}

  public static @NonNull ProjectStats projectStats() {
    final ProjectStats.CumulativeStats cumulativeStats = CumulativeStatsFactory
      .cumulativeStats()
      .toBuilder()
      .build();

    final ProjectStats.FundingDateStats fundingDateStats = FundingDateStatsFactory
      .fundingDateStats()
      .toBuilder()
      .build();

    final ProjectStats.ReferrerStats referrerStats = ReferrerStatsFactory
      .referrerStats()
      .toBuilder()
      .build();

    final ProjectStats.RewardStats rewardStats = RewardStatsFactory
      .rewardStats()
      .toBuilder()
      .build();

    final ProjectStats.VideoStats videoStats = VideoStatsFactory
      .videoStats()
      .toBuilder()
      .build();

    final List<ProjectStats.RewardStats> rewardStatsList = Collections.unmodifiableList(new ArrayList<ProjectStats.RewardStats>(Arrays.asList(rewardStats)));

    return ProjectStats.builder()
      .cumulativeStats(cumulativeStats)
      .fundingDateStats(fundingDateStats)
      .referrerStats(referrerStats)
      .rewardDistribution(rewardStatsList)
      .videoStats(videoStats)
      .build();
  }

  public static final class CumulativeStatsFactory {
    private CumulativeStatsFactory() {}

    public static @NonNull ProjectStats.CumulativeStats cumulativeStats() {
      return ProjectStats.CumulativeStats.builder()
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

    public static @NonNull ProjectStats.FundingDateStats fundingDateStats() {
      return ProjectStats.FundingDateStats.builder()
        .backersCount(10)
        .cumulativePledged(500)
        .cumulativeBackersCount(10)
        .timeInterval(new DateTime())
        .pledged(500)
        .build();
    }
  }

  public static final class ReferrerStatsFactory {
    private ReferrerStatsFactory() {}

    public static @NonNull ProjectStats.ReferrerStats referrerStats() {
      return ProjectStats.ReferrerStats.builder()
        .backersCount(10)
        .code("wots_this")
        .percentageOfDollars(50.0)
        .pledged(500)
        .referrerName("Important Referrer")
        .referrerType(ProjectStats.ReferrerType.EXTERNAL)
        .build();
    }
  }

  public static final class RewardStatsFactory {
    private RewardStatsFactory() {}

    public static @NonNull ProjectStats.RewardStats rewardStats() {
      return ProjectStats.RewardStats.builder()
        .backersCount(10)
        .rewardId(1)
        .minimum(5)
        .pledged(10)
        .build();
    }
  }

  public static final class VideoStatsFactory {
    private VideoStatsFactory() {}

    public static @NonNull ProjectStats.VideoStats videoStats() {
      return ProjectStats.VideoStats.builder()
        .externalCompletions(1000)
        .externalStarts(2000)
        .internalCompletions(500)
        .internalStarts(1000)
        .build();
    }
  }
}
