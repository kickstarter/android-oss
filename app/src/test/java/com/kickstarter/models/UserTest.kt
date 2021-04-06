package com.kickstarter.models

import com.kickstarter.mock.factories.UserFactory
import junit.framework.TestCase

class UserTest : TestCase() {
    fun testEquals_whenSecondUserNull_returnFalse() {
        val userA = UserFactory.user()
        val userB: User? = null

        assertFalse(userA == userB)
    }

    fun testEquals_whenUsersEquals_returnTrue() {
        val userA = UserFactory.user().toBuilder().id(1).build()
        val userB = UserFactory.user().toBuilder().id(1).build()

        assertTrue(userA == userB)
    }

    fun testEquals_whenUserIdDifferent_returnFalse() {
        val userA = UserFactory.user()
        val userB = UserFactory.user().toBuilder().id(999999).build()

        assertFalse(userA == userB)
    }

    fun testEquals_whenFirstUserNull_returnFalse() {
        val userA: User? = null
        val userB = UserFactory.user()

        assertFalse(userA == userB)
    }

    fun testEquals_whenIsAdminIsNull_returnFalse() {
        val locA = UserFactory.user().toBuilder().isAdmin(null).build()
        val locB = UserFactory.user().toBuilder().isAdmin(true).build()

        assertFalse(locA == locB)
    }

    fun testEquals_whenIsAdminIsDifferent_returnFalse() {
        val locA = UserFactory.user().toBuilder().isAdmin(false).build()
        val locB = UserFactory.user().toBuilder().isAdmin(true).build()

        assertFalse(locA == locB)
    }
}
