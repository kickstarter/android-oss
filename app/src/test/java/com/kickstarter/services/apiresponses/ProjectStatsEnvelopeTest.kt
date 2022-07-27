package com.kickstarter.services.apiresponses

import com.kickstarter.libs.ReferrerType
import com.kickstarter.mock.factories.ProjectStatsEnvelopeFactory
import junit.framework.TestCase
import org.joda.time.DateTime
import org.junit.Test

class ProjectStatsEnvelopeTest : TestCase() {

    @Test
    fun projectStatsEnvelopeDefaultInit() {
        val cumulativeStats = ProjectStatsEnvelopeFactory.CumulativeStatsFactory.cumulativeStats()

        val fundingDateStats = ProjectStatsEnvelopeFactory.FundingDateStatsFactory.fundingDateStats()

        val referralAggregateStats = ProjectStatsEnvelopeFactory.ReferralAggregateStatsFactory.referralAggregates()

        val referrerStats = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats()

        val rewardStats = ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats()

        val videoStats = ProjectStatsEnvelopeFactory.VideoStatsFactory.videoStats()

        val fundingDistribution = listOf(fundingDateStats, fundingDateStats)
        val referralDistribution = listOf(referrerStats, referrerStats, referrerStats)
        val rewardDistribution = listOf(rewardStats, rewardStats)

        val projectStatsEnvelope =
            ProjectStatsEnvelope
                .builder()
                .cumulative(cumulativeStats)
                .fundingDistribution(fundingDistribution)
                .referralAggregates(referralAggregateStats)
                .referralDistribution(referralDistribution)
                .rewardDistribution(rewardDistribution)
                .videoStats(videoStats)
                .build()

        assertEquals(projectStatsEnvelope.cumulative(), cumulativeStats)
        assertEquals(projectStatsEnvelope.fundingDistribution(), fundingDistribution)
        assertEquals(projectStatsEnvelope.referralAggregates(), referralAggregateStats)
        assertEquals(projectStatsEnvelope.referralDistribution(), referralDistribution)
        assertEquals(projectStatsEnvelope.rewardDistribution(), rewardDistribution)
        assertEquals(projectStatsEnvelope.videoStats(), videoStats)
    }

    @Test
    fun cumulativeStatsDefaultInit() {
        val cumulativeStats = ProjectStatsEnvelopeFactory.CumulativeStatsFactory.cumulativeStats()

        assertEquals(cumulativeStats.averagePledge(), 5f)
        assertEquals(cumulativeStats.backersCount(), 10)
        assertEquals(cumulativeStats.goal(), 1000)
        assertEquals(cumulativeStats.percentRaised(), 50f)
        assertEquals(cumulativeStats.pledged(), 500f)
    }

    @Test
    fun fundingDateStatsDefaultInit() {
        val fundingDateStats = ProjectStatsEnvelopeFactory.FundingDateStatsFactory.fundingDateStats().toBuilder().date(DateTime.now().plusMillis(3)).build()

        assertEquals(fundingDateStats.backersCount(), 10)
        assertEquals(fundingDateStats.cumulativePledged(), 500f)
        assertEquals(fundingDateStats.cumulativeBackersCount(), 10)
        assertEquals(fundingDateStats.date(), DateTime.now().plusMillis(3))
        assertEquals(fundingDateStats.pledged(), 500f)
    }

    @Test
    fun referralAggregateStatsDefaultInit() {
        val referralAggregateStats = ProjectStatsEnvelopeFactory.ReferralAggregateStatsFactory.referralAggregates()

        assertEquals(referralAggregateStats.custom(), 10f)
        assertEquals(referralAggregateStats.external(), 15f)
        assertEquals(referralAggregateStats.internal(), 20f)
    }

    @Test
    fun referrerStatsDefaultInit() {
        val referrerStats = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats()

        assertEquals(referrerStats.backersCount(), 10)
        assertEquals(referrerStats.code(), "wots_this")
        assertEquals(referrerStats.percentageOfDollars(), 50f)
        assertEquals(referrerStats.pledged(), 500f)
        assertEquals(referrerStats.referrerName(), "Important Referrer")
        assertEquals(referrerStats.referrerType(), ReferrerType.EXTERNAL.referrerType)
    }

    @Test
    fun rewardStatsDefaultInit() {
        val rewardStats = ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats()

        assertEquals(rewardStats.backersCount(), 10)
        assertEquals(rewardStats.rewardId(), 1)
        assertEquals(rewardStats.backersCount(), 5)
        assertEquals(rewardStats.backersCount(), 10f)
    }

    @Test
    fun videoStatsDefaultInit() {
        val videoStats = ProjectStatsEnvelopeFactory.VideoStatsFactory.videoStats()

        assertEquals(videoStats.externalCompletions(), 1000)
        assertEquals(videoStats.externalStarts(), 2000)
        assertEquals(videoStats.internalCompletions(), 500)
        assertEquals(videoStats.internalStarts(), 1000)
    }

    @Test
    fun cumulativeStatsNull() {
        val projectStats = ProjectStatsEnvelopeFactory.projectStatsEnvelope().toBuilder()
            .cumulative(null)
            .build()

        assertEquals(projectStats.cumulative(), null)
    }

    @Test
    fun testProjectStatsEnvelopeEquals_whenFieldsDontMatch_shouldReturnFalse() {
        val cumulativeStats1 = ProjectStatsEnvelopeFactory.CumulativeStatsFactory.cumulativeStats()

        val cumulativeStats2 = ProjectStatsEnvelopeFactory.CumulativeStatsFactory.cumulativeStats().toBuilder().averagePledge(453f).build()

        val fundingDateStats1 = ProjectStatsEnvelopeFactory.FundingDateStatsFactory.fundingDateStats().toBuilder().date(DateTime.now().plusMillis(3)).build()

        val fundingDateStats2 = ProjectStatsEnvelopeFactory.FundingDateStatsFactory.fundingDateStats().toBuilder().date(DateTime.now().plusMillis(3)).backersCount(30).build()

        val referralAggregateStats = ProjectStatsEnvelopeFactory.ReferralAggregateStatsFactory.referralAggregates()

        val referrerStats1 = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats()

        val referrerStats2 = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats().toBuilder().referrerName("another name").build()

        val rewardStats1 = ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats()

        val rewardStats2 = ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats().toBuilder().rewardId(3).build()

        val projectStatsEnvelope1 = ProjectStatsEnvelopeFactory.projectStatsEnvelope()

        val videoStats = ProjectStatsEnvelopeFactory.VideoStatsFactory.videoStats().toBuilder().externalStarts(600).build()

        val projectStatsEnvelope2 =
            projectStatsEnvelope1
                .toBuilder()
                .cumulative(cumulativeStats2)
                .referralDistribution(listOf(referrerStats1, referrerStats2, referrerStats1))
                .rewardDistribution(listOf(rewardStats2, rewardStats1))
                .build()

        val projectStatsEnvelope3 =
            projectStatsEnvelope1
                .toBuilder()
                .cumulative(cumulativeStats1)
                .fundingDistribution(listOf(fundingDateStats2, fundingDateStats1))
                .referralAggregates(referralAggregateStats)
                .rewardDistribution(listOf(rewardStats2, rewardStats2))
                .videoStats(videoStats)
                .build()

        assertFalse(projectStatsEnvelope1 == projectStatsEnvelope2)
        assertFalse(projectStatsEnvelope2 == projectStatsEnvelope3)
        assertFalse(projectStatsEnvelope3 == projectStatsEnvelope1)
    }

    @Test
    fun testCumulativeStatsEquals_whenFieldsDontMatch_shouldReturnFalse() {
        val cumulativeStats1 = ProjectStatsEnvelopeFactory.CumulativeStatsFactory.cumulativeStats()

        val cumulativeStats2 = cumulativeStats1.toBuilder().averagePledge(453f).build()

        val cumulativeStats3 =
            cumulativeStats1
                .toBuilder()
                .averagePledge(85f)
                .backersCount(200)
                .build()

        assertFalse(cumulativeStats1 == cumulativeStats2)
        assertFalse(cumulativeStats2 == cumulativeStats3)
        assertFalse(cumulativeStats3 == cumulativeStats1)
    }

    @Test
    fun testFundingDateStatsEquals_whenFieldsDontMatch_shouldReturnFalse() {
        val fundingDateStats1 = ProjectStatsEnvelopeFactory.FundingDateStatsFactory.fundingDateStats().toBuilder().date(DateTime.now().plusMillis(3)).build()

        val fundingDateStats2 = fundingDateStats1.toBuilder().date(DateTime.now().plusMillis(3)).backersCount(30).build()

        val fundingDateStats3 =
            fundingDateStats1
                .toBuilder()
                .cumulativePledged(33f)
                .cumulativeBackersCount(91)
                .pledged(23f)
                .build()

        assertFalse(fundingDateStats1 == fundingDateStats2)
        assertFalse(fundingDateStats2 == fundingDateStats3)
        assertFalse(fundingDateStats3 == fundingDateStats1)
    }

    @Test
    fun testReferralAggregateStatsEquals_whenFieldsDontMatch_shouldReturnFalse() {
        val referralAggregateStats1 = ProjectStatsEnvelopeFactory.ReferralAggregateStatsFactory.referralAggregates()

        val referralAggregateStats2 =
            referralAggregateStats1
                .toBuilder()
                .custom(57f)
                .internal(5f)
                .build()

        val referralAggregateStats3 =
            referralAggregateStats1
                .toBuilder()
                .custom(2f)
                .external(13f)
                .internal(1f)
                .build()

        assertFalse(referralAggregateStats1 == referralAggregateStats2)
        assertFalse(referralAggregateStats2 == referralAggregateStats3)
        assertFalse(referralAggregateStats3 == referralAggregateStats1)
    }

    @Test
    fun testReferrerStatsEquals_whenFieldsDontMatch_shouldReturnFalse() {
        val referrerStats1 = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats()

        val referrerStats2 =
            referrerStats1
                .toBuilder()
                .backersCount(32)
                .code("code2")
                .pledged(95f)
                .referrerName("kickstarter_name2")
                .referrerType("external")
                .build()

        val referrerStats3 =
            referrerStats1
                .toBuilder()
                .code("code3")
                .percentageOfDollars(50f)
                .pledged(39f)
                .referrerName("kickstarter_name3")
                .referrerType("custom")
                .build()

        assertFalse(referrerStats1 == referrerStats2)
        assertFalse(referrerStats3 == referrerStats2)
        assertFalse(referrerStats1 == referrerStats3)
    }

    @Test
    fun testRewardStatsEquals_whenFieldsDontMatch_shouldReturnFalse() {
        val rewardStats1 = ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats()

        val rewardStats2 =
            rewardStats1
                .toBuilder()
                .minimum(30)
                .pledged(56f)
                .build()

        val rewardStats3 =
            rewardStats1
                .toBuilder()
                .backersCount(703)
                .rewardId(232)
                .build()

        assertFalse(rewardStats1 == rewardStats2)
        assertFalse(rewardStats3 == rewardStats2)
        assertFalse(rewardStats1 == rewardStats3)
    }

    @Test
    fun testVideoStatsEquals_whenFieldsDontMatch_shouldReturnFalse() {
        val videoStats1 = ProjectStatsEnvelopeFactory.VideoStatsFactory.videoStats()

        val videoStats2 =
            videoStats1
                .toBuilder()
                .internalCompletions(30)
                .internalStarts(40)
                .build()

        val videoStats3 =
            videoStats1
                .toBuilder()
                .externalCompletions(34)
                .externalStarts(87)
                .build()

        assertFalse(videoStats1 == videoStats2)
        assertFalse(videoStats3 == videoStats2)
        assertFalse(videoStats1 == videoStats3)
    }

    @Test
    fun testProjectStatsEnvelopeEquals_whenFieldsMatch_shouldReturnTrue() {
        val projectStatsEnvelope1 = ProjectStatsEnvelopeFactory.projectStatsEnvelope()
        val projectStatsEnvelop2 = projectStatsEnvelope1

        assertTrue(projectStatsEnvelope1 == projectStatsEnvelop2)
    }

    @Test
    fun testCumulativeStatsEquals_whenFieldsMatch_shouldReturnTrue() {
        val cumulativeStats1 = ProjectStatsEnvelopeFactory.CumulativeStatsFactory.cumulativeStats()
        val cumulativeStats2 = cumulativeStats1

        assertTrue(cumulativeStats1 == cumulativeStats2)
    }

    @Test
    fun testFundingDateStatsEquals_whenFieldsMatch_shouldReturnTrue() {
        val fundingDateStats1 = ProjectStatsEnvelopeFactory.FundingDateStatsFactory.fundingDateStats()
        val fundingDateStats2 = fundingDateStats1

        assertTrue(fundingDateStats1 == fundingDateStats2)
    }

    @Test
    fun testReferralAggregateStatsEquals_whenFieldsMatch_shouldReturnTrue() {
        val referralAggregateStats1 = ProjectStatsEnvelopeFactory.ReferralAggregateStatsFactory.referralAggregates()
        val referralAggregateStats2 = referralAggregateStats1

        assertTrue(referralAggregateStats1 == referralAggregateStats2)
    }

    @Test
    fun testReferrerStatsEquals_whenFieldsMatch_shouldReturnTrue() {
        val referrerStats1 = ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats()
        val referrerStats2 = referrerStats1

        assertTrue(referrerStats1 == referrerStats2)
    }

    @Test
    fun testRewardStatsEquals_whenFieldsMatch_shouldReturnTrue() {
        val rewardStats1 = ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats()
        val rewardStats2 = rewardStats1

        assertTrue(rewardStats1 == rewardStats2)
    }

    @Test
    fun testVideoStatsEquals_whenFieldsMatch_shouldReturnTrue() {
        val videoStats1 = ProjectStatsEnvelopeFactory.VideoStatsFactory.videoStats()
        val videoStats2 = videoStats1

        assertTrue(videoStats1 == videoStats2)
    }

    @Test
    fun testProjectStatsEnvelopeToBuilder() {
        val rewardStats = ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats()

        val rewardDistribution = listOf(rewardStats, rewardStats)

        val projectStatsEnvelope =
            ProjectStatsEnvelopeFactory.projectStatsEnvelope()
                .toBuilder()
                .rewardDistribution(rewardDistribution)
                .build()

        assertEquals(projectStatsEnvelope.rewardDistribution(), rewardDistribution)
    }

    @Test
    fun testCumulativeStatsToBuilder() {
        val cumulativeStats =
            ProjectStatsEnvelopeFactory.CumulativeStatsFactory
                .cumulativeStats()
                .toBuilder()
                .averagePledge(60f)
                .build()

        assertEquals(cumulativeStats.averagePledge(), 60f)
        assertEquals(cumulativeStats.percentRaised(), 50f)
    }

    @Test
    fun testFundingDateStatsToBuilder() {
        val fundingDateStats =
            ProjectStatsEnvelopeFactory.FundingDateStatsFactory.fundingDateStats()
                .toBuilder()
                .cumulativeBackersCount(900)
                .build()

        assertEquals(fundingDateStats.cumulativeBackersCount(), 900)
        assertEquals(fundingDateStats.backersCount(), 10)
    }

    @Test
    fun testReferralAggregateStatsToBuilder() {
        val referralAggregateStats =
            ProjectStatsEnvelopeFactory.ReferralAggregateStatsFactory.referralAggregates()
                .toBuilder()
                .custom(85f)
                .build()
        assertEquals(referralAggregateStats.custom(), 85f)
        assertEquals(referralAggregateStats.external(), 15f)
    }

    @Test
    fun testReferrerStatsToBuilder() {
        val referrerStats =
            ProjectStatsEnvelopeFactory.ReferrerStatsFactory.referrerStats()
                .toBuilder()
                .referrerType("custom")
                .build()

        assertEquals(referrerStats.referrerType(), "custom")
        assertEquals(referrerStats.backersCount(), 10)
    }

    @Test
    fun testRewardStatsToBuilder() {
        val rewardStats =
            ProjectStatsEnvelopeFactory.RewardStatsFactory.rewardStats()
                .toBuilder()
                .pledged(34f)
                .build()

        assertEquals(rewardStats.backersCount(), 10)
        assertEquals(rewardStats.pledged(), 34f)
    }

    @Test
    fun testVideoStatsToBuilder() {
        val videoStats =
            ProjectStatsEnvelopeFactory.VideoStatsFactory.videoStats()
                .toBuilder()
                .externalStarts(46)
                .build()

        assertEquals(videoStats.externalCompletions(), 1000)
        assertEquals(videoStats.externalStarts(), 46)
    }

    @Test
    fun referrerTypeEnum_whenNotNull_returnsCorrectEnumValue() {
        val referrerStats =
            ProjectStatsEnvelope.ReferrerStats
                .builder()
                .referrerName("kickstarter_name")
                .referrerType("external")
                .build()

        assertEquals(ProjectStatsEnvelope.ReferrerStats.referrerTypeEnum(referrerStats.referrerType()), ReferrerType.EXTERNAL)
    }

    @Test
    fun referrerTypeEnum_whenNullOrNotRecognized_returnsDefaultValue() {
        val referrerStats =
            ProjectStatsEnvelope.ReferrerStats
                .builder()
                .referrerName("kickstarter_name")
                .build()

        assertEquals(ProjectStatsEnvelope.ReferrerStats.referrerTypeEnum(referrerStats.referrerType()), ReferrerType.KICKSTARTER)

        referrerStats.toBuilder().referrerType("kickstarter").build()

        assertEquals(ProjectStatsEnvelope.ReferrerStats.referrerTypeEnum(referrerStats.referrerType()), ReferrerType.KICKSTARTER)
    }
}
