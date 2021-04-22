package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.extensions.isLocationGermany
import com.kickstarter.models.extensions.isUserEmailVerified
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
        val userA = UserFactory.user().toBuilder().id(1).notifyMobileOfUpdates(false).build()
        val userB = UserFactory.user().toBuilder().id(1).notifyMobileOfUpdates(true).build()

        assertFalse(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfUpdates_Equals() {
        val userA = UserFactory.user().toBuilder().id(1).notifyMobileOfUpdates(true).build()
        val userB = UserFactory.user().toBuilder().id(1).notifyMobileOfUpdates(true).build()

        assertTrue(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfFollower_Different() {
        val userA = UserFactory.user().toBuilder().id(1).notifyMobileOfFollower(false).build()
        val userB = UserFactory.user().toBuilder().id(1).notifyMobileOfFollower(true).build()

        assertFalse(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfFollower_Equals() {
        val userA = UserFactory.user().toBuilder().id(1).notifyMobileOfFollower(true).build()
        val userB = UserFactory.user().toBuilder().id(1).notifyMobileOfFollower(true).build()

        assertTrue(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfBackings_Different() {
        val userA = UserFactory.user().toBuilder().id(1).notifyMobileOfBackings(false).build()
        val userB = UserFactory.user().toBuilder().id(1).notifyMobileOfBackings(true).build()

        assertFalse(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfBackings_Equals() {
        val userA = UserFactory.user().toBuilder().id(1).notifyMobileOfBackings(true).build()
        val userB = UserFactory.user().toBuilder().id(1).notifyMobileOfBackings(true).build()

        assertTrue(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfComments_Different() {
        val userA = UserFactory.user().toBuilder().id(1).notifyMobileOfComments(false).build()
        val userB = UserFactory.user().toBuilder().id(1).notifyMobileOfComments(true).build()

        assertFalse(userA == userB)
    }

    @Test
    fun isEqualsUsers_whenNotifyMobileOfComments_Equals() {
        val userA = UserFactory.user().toBuilder().id(1).notifyMobileOfComments(true).build()
        val userB = UserFactory.user().toBuilder().id(1).notifyMobileOfComments(true).build()

        assertTrue(userA == userB)
    }
}
