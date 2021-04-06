package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import org.junit.Before
import org.junit.Test

class BackingExtKtTest : KSRobolectricTestCase() {

    private lateinit var backing: Backing
    private lateinit var backingWithId: Backing
    private lateinit var backingWithReward: Backing
    private lateinit var project: Project
    private lateinit var reward: Reward

    @Before
    fun setup() {
        backing = BackingFactory.backing()
        backingWithId =
            backing
                .toBuilder()
                .rewardId(ID_MATCHING)
                .build()
        project = ProjectFactory.project()
        reward = RewardFactory.reward()
        backingWithReward = BackingFactory.backing(reward)
    }

    @Test
    fun isBacked_whenIdMismatched_returnsFalse() {
        val rewardMismatchId = RewardFactory.reward() // This creates a reward with a new generated id

        assertFalse(backingWithReward.isBacked(rewardMismatchId))
    }

    @Test
    fun isBacked_whenNoReward_returnsFalse() {
        assertFalse(backingWithReward.isBacked(RewardFactory.noReward()))
    }

    @Test
    fun isBacked_whenNoRewardBackedProject_returnsTrue() {
        val backedProject = ProjectFactory.backedProject()
        val noRewardBackedProject = BackingFactory.backing(backedProject, UserFactory.user(), RewardFactory.noReward())

        assertTrue(noRewardBackedProject.isBacked(RewardFactory.noReward()))
    }

    @Test
    fun isBacked_whenIdMatches_returnsTrue() {
        assertTrue(backingWithReward.isBacked(reward))
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
            backing
                .toBuilder()
                .reward(RewardFactory.rewardWithShipping())
                .build()

        assertFalse(backing.isShippable())
        assertTrue(backingWithShipping.isShippable())
    }

    @Test
    fun backedReward_whenRewardsNull_shouldReturnNull() {
        var backedProjectWithNoReward = ProjectFactory.backedProjectWithNoReward()

        assertNull(backing.backedReward(backedProjectWithNoReward))
    }

    @Test
    fun backedReward_whenIdNotMatching_shouldReturnNull() {
        val rewardMismatchId =
            reward
                .toBuilder()
                .id(ID_NOT_MATCHING)
                .build()
        val projectWithRewards =
            project
                .toBuilder()
                .rewards(listOf(rewardMismatchId, rewardMismatchId))
                .build()

        assertNull(backingWithId.backedReward(projectWithRewards))
    }

    @Test
    fun backedReward_whenIdMatching_shouldReturnReward() {
        val rewardIdMatching =
            reward
                .toBuilder()
                .id(ID_MATCHING)
                .build()
        val rewardMismatchId =
            reward
                .toBuilder()
                .id(ID_NOT_MATCHING)
                .build()
        val projectWithRewards =
            project
                .toBuilder()
                .rewards(listOf(rewardMismatchId, rewardIdMatching))
                .build()

        assertEquals(rewardIdMatching, backingWithId.backedReward(projectWithRewards))
    }

    companion object {
        private const val ID_MATCHING: Long = 9999
        private const val ID_NOT_MATCHING: Long = 7777
    }
}
