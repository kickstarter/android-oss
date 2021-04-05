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
}
