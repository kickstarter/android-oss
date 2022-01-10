package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.MockSharedPreferences
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.extensions.PushNotification
import com.kickstarter.models.extensions.getTraits
import com.kickstarter.models.extensions.getUniqueTraits
import com.kickstarter.models.extensions.isLocationGermany
import com.kickstarter.models.extensions.isUserEmailVerified
import com.kickstarter.models.extensions.persistTraits
import org.junit.Test

class UserExtensionTest : KSRobolectricTestCase() {

    @Test
    fun isEmailVerified_whenUserVerifiedEmail_returnTrue() {
        val user = UserFactory.user().toBuilder().isEmailVerified(true).build()
        assertTrue(user.isUserEmailVerified())
    }

    @Test
    fun isEmailVerified_whenUserNotVerifiedEmail_returnFalse() {
        val user = UserFactory.user().toBuilder().isEmailVerified(false).build()
        assertFalse(user.isUserEmailVerified())
    }

    @Test
    fun isEmailVerified_whenUserNullField_returnFalse() {
        val user = UserFactory.user().toBuilder().isEmailVerified(null).build()
        assertFalse(user.isUserEmailVerified())
    }

    @Test
    fun isLocationGermany_whenUserLocationGermany_returnTrue() {
        val germanUser = UserFactory.germanUser()
        assertTrue(germanUser.isLocationGermany())
    }

    @Test
    fun isLocationGermany_whenUserLocationMexico_returnFalse() {
        val mexicanUser = UserFactory.mexicanUser()
        assertFalse(mexicanUser.isLocationGermany())
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfUpdates_Different() {
        val userA = UserFactory.user().toBuilder().id(1L).notifyMobileOfUpdates(false).build()
        val userB = UserFactory.user().toBuilder().id(1L).notifyMobileOfUpdates(true).build()

        assertFalse(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfUpdates_Equals() {
        val userA = UserFactory.user().toBuilder().id(1L).notifyMobileOfUpdates(true).build()
        val userB = UserFactory.user().toBuilder().id(1L).notifyMobileOfUpdates(true).build()

        assertTrue(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfFollower_Different() {
        val userA = UserFactory.user().toBuilder().id(1L).notifyMobileOfFollower(false).build()
        val userB = UserFactory.user().toBuilder().id(1L).notifyMobileOfFollower(true).build()

        assertFalse(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfFollower_Equals() {
        val userA = UserFactory.user().toBuilder().id(1L).notifyMobileOfFollower(true).build()
        val userB = UserFactory.user().toBuilder().id(1L).notifyMobileOfFollower(true).build()

        assertTrue(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfBackings_Different() {
        val userA = UserFactory.user().toBuilder().id(1L).notifyMobileOfBackings(false).build()
        val userB = UserFactory.user().toBuilder().id(1L).notifyMobileOfBackings(true).build()

        assertFalse(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfBackings_Equals() {
        val userA = UserFactory.user().toBuilder().id(1L).notifyMobileOfBackings(true).build()
        val userB = UserFactory.user().toBuilder().id(1L).notifyMobileOfBackings(true).build()

        assertTrue(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfComments_Different() {
        val userA = UserFactory.user().toBuilder().id(1L).notifyMobileOfComments(false).build()
        val userB = UserFactory.user().toBuilder().id(1L).notifyMobileOfComments(true).build()

        assertFalse(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfComments_Equals() {
        val userA = UserFactory.user().toBuilder().id(1L).notifyMobileOfComments(true).build()
        val userB = UserFactory.user().toBuilder().id(1L).notifyMobileOfComments(true).build()

        assertTrue(userA == userB)
    }

    @Test
    fun userTraits_whenID_Different() {
        val userA = UserFactory.user().toBuilder()
            .id(1L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build()

        val userB = UserFactory.user().toBuilder()
            .id(0L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build()

        assertFalse(userA.getTraits() == userB.getTraits())
    }

    @Test
    fun userTraits_whenName_Different() {
        val userA = UserFactory.user().toBuilder()
            .id(1L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build()

        val userB = UserFactory.user().toBuilder()
            .id(1L)
            .name("Pikachu")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build()

        assertFalse(userA.getTraits() == userB.getTraits())
    }

    @Test
    fun userTraits_whenNumberOfTraits_Different() {
        val userA = UserFactory.user().toBuilder()
            .id(1L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyMobileOfMessages(true)
            .build()

        val userB = UserFactory.user().toBuilder()
            .id(1L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build()

        assertFalse(userA.getTraits() == userB.getTraits())
    }

    @Test
    fun userTraits_whenNotifyMobileOfMessages_Different() {
        val userA = UserFactory.user().toBuilder()
            .id(1L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build()

        val userB = UserFactory.user().toBuilder()
            .id(1L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(false)
            .build()

        assertFalse(userA.getTraits() == userB.getTraits())
    }

    @Test
    fun userTraits_whenNotifyMobileOfMarketingUpdate() {
        val userA = UserFactory.user().toBuilder()
            .id(1L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfMarketingUpdate(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build()

        val userB = UserFactory.user().toBuilder()
            .id(1L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfMarketingUpdate(false)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build()

        assertFalse(userA.getTraits() == userB.getTraits())

        val userC = UserFactory.user().toBuilder()
            .id(1L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfMarketingUpdate(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build()

        val userD = UserFactory.user().toBuilder()
            .id(1L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfMarketingUpdate(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build()

        assertTrue(userC.getTraits() == userD.getTraits())
    }

    fun userTraits_whenSameTraits_Equals() {
        val userA = UserFactory.user().toBuilder()
            .id(1L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfMarketingUpdate(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build()

        val userB = UserFactory.user().toBuilder()
            .id(1L)
            .name("")
            .notifyOfBackings(true)
            .notifyOfUpdates(true)
            .notifyOfFollower(true)
            .notifyOfFriendActivity(true)
            .notifyOfComments(true)
            .notifyOfCreatorEdu(true)
            .notifyOfMessages(true)
            .notifyOfCommentReplies(true)
            .notifyMobileOfBackings(true)
            .notifyMobileOfUpdates(true)
            .notifyMobileOfMarketingUpdate(true)
            .notifyMobileOfFollower(true)
            .notifyMobileOfFriendActivity(true)
            .notifyMobileOfComments(true)
            .notifyMobileOfPostLikes(true)
            .notifyMobileOfMessages(true)
            .build()

        assertTrue(userA.getTraits() == userB.getTraits())
    }

    @Test
    fun test_persist_traits() {
        val mockShared = MockSharedPreferences()
        val user = UserFactory.allTraitsTrue()
            .toBuilder()
            .name("Pikachu pikachez")
            .build()

        user.persistTraits(mockShared)

        val persisted = mockShared.all.toSortedMap()
        val onMemory = user.getTraits().toSortedMap()

        assert(persisted.size == 19)
        assert(onMemory.size == 19)

        persisted.forEach { entry ->
            assert(onMemory.contains(entry.key))
            assert(onMemory[entry.key].toString() == entry.value.toString())
        }
    }

    @Test
    fun test_uniqueTraits_Unique_Change() {
        val mockShared = MockSharedPreferences()
        val user = UserFactory.allTraitsTrue()
            .toBuilder()
            .name("Pikachu pikachez")
            .build()

        user.persistTraits(mockShared)

        val updatedUser = user.toBuilder().notifyMobileOfBackings(false).build()

        val uniqueTraits = updatedUser.getUniqueTraits(mockShared)

        assertTrue(uniqueTraits.size == 1)
        assertTrue(uniqueTraits.containsKey(PushNotification.PUSH_BACKINGS.field))
        assert(uniqueTraits[PushNotification.PUSH_BACKINGS.field] == false)
    }

    @Test
    fun test_uniqueTraits_Several_change() {
        val mockShared = MockSharedPreferences()
        val user = UserFactory.allTraitsTrue()
            .toBuilder()
            .name("Pikachu pikachez")
            .build()

        user.persistTraits(mockShared)

        val updatedUser = user.toBuilder()
            .notifyMobileOfBackings(false)
            .notifyMobileOfMarketingUpdate(false)
            .notifyMobileOfFollower(false)
            .build()

        val uniqueTraits = updatedUser.getUniqueTraits(mockShared)

        assertTrue(uniqueTraits.size == 3)
        assertTrue(uniqueTraits.containsKey(PushNotification.PUSH_BACKINGS.field))
        assertTrue(uniqueTraits.containsKey(PushNotification.PUSH_MARKETING.field))
        assertTrue(uniqueTraits.containsKey(PushNotification.PUSH_FOLLOWER.field))
        assert(uniqueTraits[PushNotification.PUSH_BACKINGS.field] == false)
        assert(uniqueTraits[PushNotification.PUSH_MARKETING.field] == false)
        assert(uniqueTraits[PushNotification.PUSH_FOLLOWER.field] == false)
    }

    @Test
    fun test_uniqueTraits_None_Change() {
        val mockShared = MockSharedPreferences()
        val user = UserFactory.allTraitsTrue()
            .toBuilder()
            .name("Pikachu pikachez")
            .build()

        user.persistTraits(mockShared)

        val uniqueTraits = user.getUniqueTraits(mockShared)

        assertTrue(uniqueTraits.isEmpty())
    }
}
