package com.kickstarter.services.apiresponses

import com.kickstarter.libs.ReferrerType
import junit.framework.TestCase
import org.joda.time.DateTime
import org.junit.Test

class ProjectStatsEnvelopeTest : TestCase() {

    @Test
    fun projectStatsEnvelopeDefaultInit(){
        val cumulativeStats =
            ProjectStatsEnvelope.CumulativeStats
                .builder()
                .averagePledge(35f)
                .backersCount(53)
                .goal(2343)
                .percentRaised(55f)
                .pledged(34f)
                .build()

        val fundingDateStats =
            ProjectStatsEnvelope.FundingDateStats
                .builder()
                .backersCount(837)
                .cumulativePledged(3490f)
                .cumulativeBackersCount(875)
                .date(DateTime.now().plusMillis(3))
                .pledged(243f)
                .build()

        val referralAggregateStats =
            ProjectStatsEnvelope.ReferralAggregateStats
                .builder()
                .custom(23f)
                .external(123f)
                .internal(12f)
                .build()

        val referrerStats =
            ProjectStatsEnvelope.ReferrerStats
                .builder()
                .backersCount(52)
                .code("code1")
                .percentageOfDollars(24f)
                .pledged(395f)
                .referrerName("kickstarter_name")
                .referrerType("kickstarter")
                .build()

        val rewardStats =
            ProjectStatsEnvelope.RewardStats
                .builder()
                .backersCount(24)
                .rewardId(2312)
                .minimum(10)
                .pledged(5634f)
                .build()

        val videoStats =
            ProjectStatsEnvelope.VideoStats
                .builder()
                .externalCompletions(5)
                .externalStarts(10)
                .internalCompletions(15)
                .internalStarts(20)
                .build()

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
        val cumulativeStats =
            ProjectStatsEnvelope.CumulativeStats
                .builder()
                .averagePledge(35f)
                .backersCount(53)
                .goal(2343)
                .percentRaised(55f)
                .pledged(34f)
                .build()

        assertEquals(cumulativeStats.averagePledge(), 35f)
        assertEquals(cumulativeStats.backersCount(), 53)
        assertEquals(cumulativeStats.goal(), 2343)
        assertEquals(cumulativeStats.percentRaised(), 55f)
        assertEquals(cumulativeStats.pledged(), 34f)
    }

    @Test
    fun fundingDateStatsDefaultInit() {
        val fundingDateStats =
            ProjectStatsEnvelope.FundingDateStats
                .builder()
                .backersCount(837)
                .cumulativePledged(3490f)
                .cumulativeBackersCount(875)
                .date(DateTime.now().plusMillis(3))
                .pledged(243f)
                .build()

        assertEquals(fundingDateStats.backersCount(), 837)
        assertEquals(fundingDateStats.cumulativePledged(), 3490f)
        assertEquals(fundingDateStats.cumulativeBackersCount(), 875)
        assertEquals(fundingDateStats.date(), DateTime.now().plusMillis(3))
        assertEquals(fundingDateStats.pledged(), 243f)
    }

    @Test
    fun referralAggregateStatsDefaultInit() {
        val referralAggregateStats =
            ProjectStatsEnvelope.ReferralAggregateStats
                .builder()
                .custom(23f)
                .external(123f)
                .internal(12f)
                .build()

        assertEquals(referralAggregateStats.custom(), 23f)
        assertEquals(referralAggregateStats.external(), 123f)
        assertEquals(referralAggregateStats.internal(), 12f)
    }

    @Test
    fun referrerStatsDefaultInit() {
        val referrerStats =
            ProjectStatsEnvelope.ReferrerStats
                .builder()
                .backersCount(52)
                .code("code1")
                .percentageOfDollars(24f)
                .pledged(395f)
                .referrerName("kickstarter_name")
                .referrerType("kickstarter")
                .build()

        assertEquals(referrerStats.backersCount(), 52)
        assertEquals(referrerStats.code(), "code1")
        assertEquals(referrerStats.percentageOfDollars(), 24f)
        assertEquals(referrerStats.pledged(), 395f)
        assertEquals(referrerStats.referrerName(), "kickstarter_name")
        assertEquals(referrerStats.referrerType(), "kickstarter")
    }

    @Test
    fun rewardStatsDefaultInit() {
        val rewardStats =
            ProjectStatsEnvelope.RewardStats
                .builder()
                .backersCount(24)
                .rewardId(2312)
                .minimum(10)
                .pledged(5634f)
                .build()

        assertEquals(rewardStats.backersCount(), 24)
        assertEquals(rewardStats.rewardId(), 2312)
        assertEquals(rewardStats.backersCount(), 10)
        assertEquals(rewardStats.backersCount(), 5634f)
    }

    @Test
    fun videoStatsDefaultInit(){
        val videoStats =
            ProjectStatsEnvelope.VideoStats
                .builder()
                .externalCompletions(5)
                .externalStarts(10)
                .internalCompletions(15)
                .internalStarts(20)
                .build()

        assertEquals(videoStats.externalCompletions(), 5)
        assertEquals(videoStats.externalStarts(), 10)
        assertEquals(videoStats.internalCompletions(), 15)
        assertEquals(videoStats.internalStarts(), 20)
    }

    @Test
    fun testProjectStatsEnvelopeEquals_whenFieldsDontMatch_shouldReturnFalse(){
        val cumulativeStats1 =
            ProjectStatsEnvelope.CumulativeStats
                .builder()
                .averagePledge(35f)
                .backersCount(53)
                .goal(2343)
                .percentRaised(55f)
                .pledged(34f)
                .build()

        val cumulativeStats2 =
            ProjectStatsEnvelope.CumulativeStats
                .builder()
                .averagePledge(345f)
                .backersCount(79)
                .goal(2343)
                .percentRaised(55f)
                .pledged(34f)
                .build()

        val fundingDateStats1 =
            ProjectStatsEnvelope.FundingDateStats
                .builder()
                .backersCount(837)
                .cumulativePledged(3490f)
                .cumulativeBackersCount(875)
                .date(DateTime.now().plusMillis(3))
                .pledged(243f)
                .build()

        val fundingDateStats2 =
            ProjectStatsEnvelope.FundingDateStats
                .builder()
                .backersCount(12343)
                .cumulativePledged(303f)
                .cumulativeBackersCount(921)
                .date(DateTime.now().plusMillis(3))
                .pledged(243f)
                .build()

        val referralAggregateStats =
            ProjectStatsEnvelope.ReferralAggregateStats
                .builder()
                .custom(23f)
                .external(123f)
                .internal(12f)
                .build()

        val referrerStats1 =
            ProjectStatsEnvelope.ReferrerStats
                .builder()
                .backersCount(52)
                .code("code1")
                .percentageOfDollars(24f)
                .pledged(395f)
                .referrerName("kickstarter_name")
                .referrerType("kickstarter")
                .build()

        val referrerStats2 =
            ProjectStatsEnvelope.ReferrerStats
                .builder()
                .backersCount(52)
                .code("code1")
                .percentageOfDollars(24f)
                .pledged(395f)
                .referrerName("kickstarter_name")
                .referrerType("kickstarter")
                .build()

        val rewardStats1 =
            ProjectStatsEnvelope.RewardStats
                .builder()
                .backersCount(24)
                .rewardId(2312)
                .minimum(10)
                .pledged(5634f)
                .build()

        val rewardStats2 =
            ProjectStatsEnvelope.RewardStats
                .builder()
                .backersCount(234)
                .rewardId(857)
                .minimum(910)
                .pledged(5634f)
                .build()

        val videoStats =
            ProjectStatsEnvelope.VideoStats
                .builder()
                .externalCompletions(5)
                .externalStarts(10)
                .internalCompletions(15)
                .internalStarts(20)
                .build()

        val projectStatsEnvelope1 =
            ProjectStatsEnvelope
                .builder()
                .cumulative(cumulativeStats1)
                .fundingDistribution(listOf(fundingDateStats1, fundingDateStats1))
                .referralAggregates(referralAggregateStats)
                .referralDistribution(listOf(referrerStats1, referrerStats1, referrerStats1))
                .rewardDistribution(listOf(rewardStats1, rewardStats1))
                .videoStats(videoStats)
                .build()

        val projectStatsEnvelope2 =
            ProjectStatsEnvelope
                .builder()
                .cumulative(cumulativeStats2)
                .fundingDistribution(listOf(fundingDateStats1, fundingDateStats2))
                .referralAggregates(referralAggregateStats)
                .referralDistribution(listOf(referrerStats1, referrerStats2, referrerStats1))
                .rewardDistribution(listOf(rewardStats2, rewardStats1))
                .videoStats(videoStats)
                .build()

        val projectStatsEnvelope3 =
            ProjectStatsEnvelope
                .builder()
                .cumulative(cumulativeStats1)
                .fundingDistribution(listOf(fundingDateStats2, fundingDateStats2))
                .referralAggregates(referralAggregateStats)
                .referralDistribution(listOf(referrerStats2, referrerStats2, referrerStats2))
                .rewardDistribution(listOf(rewardStats2, rewardStats2))
                .videoStats(videoStats)
                .build()

        assertFalse(projectStatsEnvelope1 == projectStatsEnvelope2)
        assertFalse(projectStatsEnvelope2 == projectStatsEnvelope3)
        assertFalse(projectStatsEnvelope3 == projectStatsEnvelope1)
    }

    @Test
    fun testCumulativeStatsEquals_whenFieldsDontMatch_shouldReturnFalse(){
        val cumulativeStats1 =
            ProjectStatsEnvelope.CumulativeStats
                .builder()
                .averagePledge(35f)
                .backersCount(53)
                .goal(2343)
                .percentRaised(55f)
                .pledged(34f)
                .build()

        val cumulativeStats2 =
            cumulativeStats1
                .toBuilder()
                .goal(2343)
                .percentRaised(55f)
                .pledged(34f)
                .build()

        val cumulativeStats3=
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
    fun testFundingDateStatsEquals_whenFieldsDontMatch_shouldReturnFalse(){
        val fundingDateStats1 =
            ProjectStatsEnvelope.FundingDateStats
                .builder()
                .backersCount(837)
                .cumulativePledged(3490f)
                .cumulativeBackersCount(875)
                .date(DateTime.now().plusMillis(3))
                .pledged(243f)
                .build()

        val fundingDateStats2 =
            fundingDateStats1
                .toBuilder()
                .backersCount(123)
                .cumulativeBackersCount(843)
                .pledged(43f)
                .build()

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
    fun testReferralAggregateStatsEquals_whenFieldsDontMatch_shouldReturnFalse(){
        val referralAggregateStats1 =
            ProjectStatsEnvelope.ReferralAggregateStats
                .builder()
                .custom(23f)
                .external(123f)
                .internal(12f)
                .build()

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
    fun testReferrerStatsEquals_whenFieldsDontMatch_shouldReturnFalse(){
        val referrerStats1 =
            ProjectStatsEnvelope.ReferrerStats
                .builder()
                .backersCount(52)
                .code("code1")
                .percentageOfDollars(24f)
                .pledged(395f)
                .referrerName("kickstarter_name1")
                .referrerType("kickstarter")
                .build()

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
    fun testRewardStatsEquals_whenFieldsDontMatch_shouldReturnFalse(){
        val rewardStats1 =
            ProjectStatsEnvelope.RewardStats
                .builder()
                .backersCount(24)
                .rewardId(2312)
                .minimum(10)
                .pledged(5634f)
                .build()

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
    fun testVideoStatsEquals_whenFieldsDontMatch_shouldReturnFalse(){
        val videoStats1 =
            ProjectStatsEnvelope.VideoStats
                .builder()
                .externalCompletions(5)
                .externalStarts(10)
                .internalCompletions(15)
                .internalStarts(20)
                .build()

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
    fun testProjectStatsEnvelopeEquals_whenFieldsMatch_shouldReturnTrue(){
        val cumulativeStats =
            ProjectStatsEnvelope.CumulativeStats
                .builder()
                .averagePledge(35f)
                .backersCount(53)
                .goal(2343)
                .percentRaised(55f)
                .pledged(34f)
                .build()

        val fundingDateStats =
            ProjectStatsEnvelope.FundingDateStats
                .builder()
                .backersCount(837)
                .cumulativePledged(3490f)
                .cumulativeBackersCount(875)
                .date(DateTime.now().plusMillis(3))
                .pledged(243f)
                .build()

        val referralAggregateStats =
            ProjectStatsEnvelope.ReferralAggregateStats
                .builder()
                .custom(23f)
                .external(123f)
                .internal(12f)
                .build()

        val referrerStats =
            ProjectStatsEnvelope.ReferrerStats
                .builder()
                .backersCount(52)
                .code("code1")
                .percentageOfDollars(24f)
                .pledged(395f)
                .referrerName("kickstarter_name")
                .referrerType("kickstarter")
                .build()

        val rewardStats =
            ProjectStatsEnvelope.RewardStats
                .builder()
                .backersCount(24)
                .rewardId(2312)
                .minimum(10)
                .pledged(5634f)
                .build()

        val videoStats =
            ProjectStatsEnvelope.VideoStats
                .builder()
                .externalCompletions(5)
                .externalStarts(10)
                .internalCompletions(15)
                .internalStarts(20)
                .build()

        val fundingDistribution = listOf(fundingDateStats, fundingDateStats)
        val referralDistribution = listOf(referrerStats, referrerStats, referrerStats)
        val rewardDistribution = listOf(rewardStats, rewardStats)

        val projectStatsEnvelope1 =
            ProjectStatsEnvelope
                .builder()
                .cumulative(cumulativeStats)
                .fundingDistribution(fundingDistribution)
                .referralAggregates(referralAggregateStats)
                .referralDistribution(referralDistribution)
                .rewardDistribution(rewardDistribution)
                .videoStats(videoStats)
                .build()

        val projectStatsEnvelop2 = projectStatsEnvelope1

        assertTrue(projectStatsEnvelope1 == projectStatsEnvelop2)
    }

    @Test
    fun testCumulativeStatsEquals_whenFieldsMatch_shouldReturnTrue(){
        val cumulativeStats1 =
            ProjectStatsEnvelope.CumulativeStats
                .builder()
                .averagePledge(35f)
                .backersCount(53)
                .goal(2343)
                .percentRaised(55f)
                .pledged(34f)
                .build()

        val cumulativeStats2 = cumulativeStats1

        assertTrue(cumulativeStats1 == cumulativeStats2)

    }

    @Test
    fun testFundingDateStatsEquals_whenFieldsMatch_shouldReturnTrue(){
        val fundingDateStats1 =
            ProjectStatsEnvelope.FundingDateStats
                .builder()
                .backersCount(837)
                .cumulativePledged(3490f)
                .cumulativeBackersCount(875)
                .date(DateTime.now().plusMillis(3))
                .pledged(243f)
                .build()

        val fundingDateStats2 = fundingDateStats1

        assertTrue(fundingDateStats1 == fundingDateStats2)
    }

    @Test
    fun testReferralAggregateStatsEquals_whenFieldsMatch_shouldReturnTrue(){
        val referralAggregateStats1 =
            ProjectStatsEnvelope.ReferralAggregateStats
                .builder()
                .custom(23f)
                .external(123f)
                .internal(12f)
                .build()

        val referralAggregateStats2 = referralAggregateStats1

        assertTrue(referralAggregateStats1 == referralAggregateStats2)
    }

    @Test
    fun testReferrerStatsEquals_whenFieldsMatch_shouldReturnTrue(){
        val referrerStats1 =
            ProjectStatsEnvelope.ReferrerStats
                .builder()
                .backersCount(52)
                .code("code1")
                .percentageOfDollars(24f)
                .pledged(395f)
                .referrerName("kickstarter_name")
                .referrerType("kickstarter")
                .build()

        val referrerStats2 = referrerStats1

        assertTrue(referrerStats1 == referrerStats2)
    }

    @Test
    fun testRewardStatsEquals_whenFieldsMatch_shouldReturnTrue(){
        val rewardStats1 =
            ProjectStatsEnvelope.RewardStats
                .builder()
                .backersCount(24)
                .rewardId(2312)
                .minimum(10)
                .pledged(5634f)
                .build()

        val rewardStats2 = rewardStats1

        assertTrue(rewardStats1 == rewardStats2)
    }

    @Test
    fun testVideoStatsEquals_whenFieldsMatch_shouldReturnTrue(){
        val videoStats1 =
            ProjectStatsEnvelope.VideoStats
                .builder()
                .externalCompletions(5)
                .externalStarts(10)
                .internalCompletions(15)
                .internalStarts(20)
                .build()

        val videoStats2 = videoStats1

        assertTrue(videoStats1 == videoStats2)
    }

    @Test
    fun testProjectStatsEnvelopeToBuilder(){
        val rewardStats =
            ProjectStatsEnvelope.RewardStats
                .builder()
                .backersCount(24)
                .rewardId(2312)
                .minimum(10)
                .pledged(5634f)
                .build()

        val rewardDistribution = listOf(rewardStats, rewardStats)

        val projectStatsEnvelope =
            ProjectStatsEnvelope
                .builder()
                .cumulative(ProjectStatsEnvelope.CumulativeStats.builder().build())
                .fundingDistribution(listOf(ProjectStatsEnvelope.FundingDateStats.builder().build()))
                .referralAggregates(ProjectStatsEnvelope.ReferralAggregateStats.builder().build())
                .referralDistribution(listOf(ProjectStatsEnvelope.ReferrerStats.builder().build()))
                .rewardDistribution(listOf(ProjectStatsEnvelope.RewardStats.builder().build()))
                .videoStats(ProjectStatsEnvelope.VideoStats.builder().build())
                .build()
                .toBuilder()
                .rewardDistribution(rewardDistribution)
                .build()

        assertEquals(projectStatsEnvelope.rewardDistribution(), rewardDistribution)
    }

    /**
     * ProjectStatsEnvelope
     * CumulativeStats
     * FundingDateStats
     * ReferralAggregateStats
     * ReferrerStats
     * RewardStats
     * VideoStats
     **/

    @Test
    fun testCumulativeStatsToBuilder(){
        val cumulativeStats =
            ProjectStatsEnvelope.CumulativeStats
                .builder()
                .averagePledge(35f)
                .backersCount(53)
                .goal(2343)
                .percentRaised(55f)
                .pledged(34f)
                .build()
                .toBuilder()
                .averagePledge(60f)
                .build()

        assertEquals(cumulativeStats.averagePledge(), 60f)
        assertEquals(cumulativeStats.percentRaised(), 55f)
    }

    @Test
    fun testFundingDateStatsToBuilder(){
        val fundingDateStats =
            ProjectStatsEnvelope.FundingDateStats
                .builder()
                .backersCount(837)
                .cumulativePledged(3490f)
                .cumulativeBackersCount(875)
                .date(DateTime.now().plusMillis(3))
                .pledged(243f)
                .build()
                .toBuilder()
                .cumulativeBackersCount(900)
                .build()

        assertEquals(fundingDateStats.cumulativeBackersCount(), 900)
        assertEquals(fundingDateStats.backersCount(), 837)
    }

    @Test
    fun testReferralAggregateStatsToBuilder(){
        val referralAggregateStats =
            ProjectStatsEnvelope.ReferralAggregateStats
                .builder()
                .custom(23f)
                .external(123f)
                .internal(12f)
                .build()
                .toBuilder()
                .custom(85f)
                .build()

        assertEquals(referralAggregateStats.custom(), 85f)
        assertEquals(referralAggregateStats.external(), 123f)
    }

    @Test
    fun testReferrerStatsToBuilder(){
        val referrerStats =
            ProjectStatsEnvelope.ReferrerStats
                .builder()
                .backersCount(52)
                .code("code1")
                .percentageOfDollars(24f)
                .pledged(395f)
                .referrerName("kickstarter_name")
                .referrerType("kickstarter")
                .build()
                .toBuilder()
                .referrerType("custom")
                .build()

        assertEquals(referrerStats.referrerType(), "custom")
        assertEquals(referrerStats.backersCount(), 52)
    }

    @Test
    fun testRewardStatsToBuilder(){
        val rewardStats =
            ProjectStatsEnvelope.RewardStats
                .builder()
                .backersCount(24)
                .rewardId(2312)
                .minimum(10)
                .pledged(5634f)
                .build()
                .toBuilder()
                .pledged(34f)
                .build()

        assertEquals(rewardStats.backersCount(), 24)
        assertEquals(rewardStats.pledged(), 34f)
    }

    @Test
    fun testVideoStatsToBuilder(){
        val videoStats =
            ProjectStatsEnvelope.VideoStats
                .builder()
                .externalCompletions(5)
                .externalStarts(10)
                .internalCompletions(15)
                .internalStarts(20)
                .build()
                .toBuilder()
                .externalStarts(46)
                .build()

        assertEquals(videoStats.externalCompletions(), 5)
        assertEquals(videoStats.externalStarts(), 46)
    }

    @Test
    fun referrerTypeEnum_whenNotNull_returnsCorrectEnumValue(){
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