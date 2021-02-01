package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Backing
import org.junit.Test

class BackingExtKtTest : KSRobolectricTestCase() {

    @Test
    fun testIsBacked() {
        val backedProject = ProjectFactory.backedProject()
        val reward = RewardFactory.reward()
        val backing = BackingFactory.backing(reward)
        val noRewardBackedProject = BackingFactory.backing(backedProject, UserFactory.user(), RewardFactory.noReward())

        assertTrue(backing.isBacked(reward))
        assertFalse(backing.isBacked(RewardFactory.reward()))
        assertFalse(backing.isBacked(RewardFactory.noReward()))
        assertTrue(noRewardBackedProject.isBacked(RewardFactory.noReward()))
    }

    @Test
    fun testIsErrored() {
        assertFalse(BackingFactory.backing(Backing.STATUS_CANCELED).isErrored())
        assertFalse(BackingFactory.backing(Backing.STATUS_COLLECTED).isErrored())
        assertFalse(BackingFactory.backing(Backing.STATUS_DROPPED).isErrored())
        assertTrue(BackingFactory.backing(Backing.STATUS_ERRORED).isErrored())
        assertFalse(BackingFactory.backing(Backing.STATUS_PLEDGED).isErrored())
        assertFalse(BackingFactory.backing(Backing.STATUS_PREAUTH).isErrored())
    }

    @Test
    fun testIsShippable() {
        val backingWithShipping =
                BackingFactory
                    .backing()
                    .toBuilder()
                    .reward(RewardFactory.rewardWithShipping())
                    .build()
        assertTrue(backingWithShipping.isShippable())
        assertFalse(BackingFactory.backing().isShippable())
    }
}
