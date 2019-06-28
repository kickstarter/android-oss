package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import org.joda.time.DateTime
import org.joda.time.MutableDateTime
import org.junit.Test

class RewardViewUtilsTest : KSRobolectricTestCase() {

    @Test
    fun testCheckBackgroundDrawable() {
        assertEquals(R.drawable.circle_blue_alpha_6, RewardViewUtils.checkBackgroundDrawable(ProjectFactory.project()))
        assertEquals(R.drawable.circle_grey_300, RewardViewUtils.checkBackgroundDrawable(ProjectFactory.successfulProject()))
    }

    @Test
    fun testDeadlineCountdownDetailWithDaysLeft() {
        val context = context()
        val ksString = ksString()
        val date = DateTime.now().toDate()
        val currentDate = MutableDateTime(date)
        currentDate.addDays(31)

        val rewardWith30DaysRemaining = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(RewardViewUtils.deadlineCountdownDetail(rewardWith30DaysRemaining, context, ksString), "days to go")
    }

    @Test
    fun testDeadlineCountdownDetailWithHoursLeft() {
        val context = context()
        val ksString = ksString()
        val date = DateTime.now().toDate()
        val currentDate = MutableDateTime(date)
        currentDate.addHours(3)

        val rewardWith30DaysRemaining = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(RewardViewUtils.deadlineCountdownDetail(rewardWith30DaysRemaining, context, ksString), "hours to go")
    }

    @Test
    fun testDeadlineCountdownDetailWithSecondsLeft() {
        val context = context()
        val ksString = ksString()
        val date = DateTime.now().toDate()
        val currentDate = MutableDateTime(date)
        currentDate.addSeconds(3)

        val rewardWith30DaysRemaining = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(RewardViewUtils.deadlineCountdownDetail(rewardWith30DaysRemaining, context, ksString), "secs to go")
    }

    @Test
    fun testDeadlineCountdownUnitWithDaysLeft() {
        val context = context()
        val date = DateTime.now().toDate()
        val currentDate = MutableDateTime(date)
        currentDate.addDays(31)

        val rewardWithDaysRemaining = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(RewardViewUtils.deadlineCountdownUnit(rewardWithDaysRemaining, context), "days")
    }

    @Test
    fun testDeadlineCountdownUnitWithHoursLeft() {
        val context = context()
        val date = DateTime.now().toDate()
        val currentDate = MutableDateTime(date)
        currentDate.addHours(3)

        val rewardWithHoursRemaining = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(RewardViewUtils.deadlineCountdownUnit(rewardWithHoursRemaining, context), "hours")
    }

    @Test
    fun testDeadlineCountdownUnitWithMinutesLeft() {
        val context = context()
        val date = DateTime.now().toDate()
        val currentDate = MutableDateTime(date)
        currentDate.addMinutes(3)

        val rewardWithMinutesRemaining = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(RewardViewUtils.deadlineCountdownUnit(rewardWithMinutesRemaining, context), "mins")
    }

    @Test
    fun testDeadlineCountdownUnitWithSecondsLeft() {
        val context = context()
        val date = DateTime.now().toDate()
        val currentDate = MutableDateTime(date)
        currentDate.addSeconds(30)

        val rewardWithSecondsRemaining = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(RewardViewUtils.deadlineCountdownUnit(rewardWithSecondsRemaining, context), "secs")
    }

    @Test
    fun testDeadlineCountdownValueWithMinutesLeft() {
        val date = DateTime.now().toDate()
        val currentDate = MutableDateTime(date)
        currentDate.addSeconds(300)

        val rewardWithMinutesRemaining = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(RewardViewUtils.deadlineCountdownValue(rewardWithMinutesRemaining), 5)
    }

    @Test
    fun testDeadlineCountdownValueWithHoursLeft() {
        val date = DateTime.now().toDate()
        val currentDate = MutableDateTime(date)
        currentDate.addSeconds(3600)

        val rewardWithHoursRemaining = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(RewardViewUtils.deadlineCountdownValue(rewardWithHoursRemaining), 60)
    }

    @Test
    fun testDeadlineCountdownValueWithDaysLeft() {
        val date = DateTime.now().toDate()
        val currentDate = MutableDateTime(date)
        currentDate.addSeconds(86400)

        val rewardWithDaysRemaining = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(RewardViewUtils.deadlineCountdownValue(rewardWithDaysRemaining), 24)
    }


    @Test
    fun testDeadlineCountdownValueWithSecondsLeft() {
        val date = DateTime.now().toDate()
        val currentDate = MutableDateTime(date)
        currentDate.addSeconds(30)

        val rewardWithSecondsRemaining = RewardFactory.reward().toBuilder()
                .endsAt(currentDate.toDateTime())
                .build()
        assertEquals(RewardViewUtils.deadlineCountdownValue(rewardWithSecondsRemaining), 30)
    }

    @Test
    fun testPledgeButtonColor() {
        assertEquals(R.color.button_pledge_live, RewardViewUtils.pledgeButtonColor(ProjectFactory.project(), RewardFactory.reward()))
        val backedProject = ProjectFactory.backedProject()
        val backedReward = backedProject.backing()?.reward()?: RewardFactory.reward()
        assertEquals(R.color.button_pledge_manage, RewardViewUtils.pledgeButtonColor(backedProject, backedReward))
        val backedSuccessfulProject = ProjectFactory.backedProject().toBuilder().state(Project.STATE_SUCCESSFUL).build()
        val backedSuccessfulReward = backedProject.backing()?.reward()?: RewardFactory.reward()
        assertEquals(R.color.button_pledge_ended, RewardViewUtils.pledgeButtonColor(backedSuccessfulProject, backedSuccessfulReward))
        assertEquals(R.color.button_pledge_ended, RewardViewUtils.pledgeButtonColor(ProjectFactory.successfulProject(), RewardFactory.reward()))
    }

    @Test
    fun testPledgeButtonAlternateText() {
        assertEquals(R.string.No_longer_available, RewardViewUtils.pledgeButtonAlternateText(ProjectFactory.project(), RewardFactory.ended()))
        assertEquals(R.string.No_longer_available, RewardViewUtils.pledgeButtonAlternateText(ProjectFactory.project(), RewardFactory.limitReached()))
        val backedProject = ProjectFactory.backedProject()
        val backedReward = backedProject.backing()?.reward()?: RewardFactory.reward()
        assertEquals(R.string.Manage_your_pledge, RewardViewUtils.pledgeButtonAlternateText(backedProject, backedReward))
        assertEquals(R.string.Select_this_instead, RewardViewUtils.pledgeButtonAlternateText(backedProject, RewardFactory.reward()))
        val backedSuccessfulProject = ProjectFactory.backedProject().toBuilder().state(Project.STATE_SUCCESSFUL).build()
        val backedSuccessfulReward = backedSuccessfulProject.backing()?.reward()?: RewardFactory.reward()
        assertEquals(R.string.View_your_pledge, RewardViewUtils.pledgeButtonAlternateText(backedSuccessfulProject, backedSuccessfulReward))
    }

    @Test
    fun testTimeInSecondsUntilDeadline() {
        val date = DateTime.now().toDate()
        val currentDate = MutableDateTime(date)
        currentDate.addSeconds(120)
        val reward = RewardFactory.reward().toBuilder().endsAt(currentDate.toDateTime()).build()
        val timeInSecondsUntilDeadline = RewardViewUtils.timeInSecondsUntilDeadline(reward)
        assertEquals(timeInSecondsUntilDeadline, 120)
    }

}
