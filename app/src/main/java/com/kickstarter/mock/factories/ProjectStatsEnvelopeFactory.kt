package com.kickstarter.mock.factories

import com.kickstarter.libs.ReferrerType
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.CumulativeStats
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.FundingDateStats
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.ReferralAggregateStats
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.ReferrerStats
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.RewardStats
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope.VideoStats
import org.joda.time.DateTime

object ProjectStatsEnvelopeFactory {
    @JvmStatic
    fun projectStatsEnvelope(): ProjectStatsEnvelope {
        val cumulativeStats = CumulativeStatsFactory.cumulativeStats()
        val fundingDateStats = FundingDateStatsFactory.fundingDateStats()
        val referrerStats = ReferrerStatsFactory.referrerStats()
        val referralAggregates = ReferralAggregateStatsFactory.referralAggregates()
        val rewardStats = RewardStatsFactory.rewardStats()
        val videoStats = VideoStatsFactory.videoStats()
        val fundingDateStatsList = listOf(fundingDateStats)
        val rewardStatsList = listOf(rewardStats)
        val referrerStatsList = listOf(referrerStats)
        return ProjectStatsEnvelope.builder()
            .cumulative(cumulativeStats)
            .fundingDistribution(fundingDateStatsList)
            .referralAggregates(referralAggregates)
            .referralDistribution(referrerStatsList)
            .rewardDistribution(rewardStatsList)
            .videoStats(videoStats)
            .build()
    }

    object CumulativeStatsFactory {
        @JvmStatic
        fun cumulativeStats(): CumulativeStats {
            return CumulativeStats.builder()
                .averagePledge(5f)
                .backersCount(10)
                .goal(1000)
                .percentRaised(50f)
                .pledged(500f)
                .build()
        }
    }

    object FundingDateStatsFactory {
        fun fundingDateStats(): FundingDateStats {
            return FundingDateStats.builder()
                .backersCount(10)
                .cumulativePledged(500f)
                .cumulativeBackersCount(10)
                .date(DateTime())
                .pledged(500f)
                .build()
        }
    }

    object ReferralAggregateStatsFactory {
        @JvmStatic
        fun referralAggregates(): ReferralAggregateStats {
            return ReferralAggregateStats.builder()
                .custom(10f)
                .external(15f)
                .internal(20f)
                .build()
        }
    }

    object ReferrerStatsFactory {
        @JvmStatic
        fun referrerStats(): ReferrerStats {
            return ReferrerStats.builder()
                .backersCount(10)
                .code("wots_this")
                .percentageOfDollars(50f)
                .pledged(500f)
                .referrerName("Important Referrer")
                .referrerType(ReferrerType.EXTERNAL.referrerType)
                .build()
        }
    }

    object RewardStatsFactory {
        @JvmStatic
        fun rewardStats(): RewardStats {
            return RewardStats.builder()
                .backersCount(10)
                .rewardId(1)
                .minimum(5)
                .pledged(10f)
                .build()
        }
    }

    object VideoStatsFactory {
        fun videoStats(): VideoStats {
            return VideoStats.builder()
                .externalCompletions(1000)
                .externalStarts(2000)
                .internalCompletions(500)
                .internalStarts(1000)
                .build()
        }
    }
}
