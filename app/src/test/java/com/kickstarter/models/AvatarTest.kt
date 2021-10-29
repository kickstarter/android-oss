package com.kickstarter.models

import com.kickstarter.mock.factories.AvatarFactory
import junit.framework.TestCase

class AvatarTest: TestCase() {

    fun testEquals_whenSecondAvatarDifferent_returnFalse() {
        val avatarA = AvatarFactory.avatar()
        val avatarB = AvatarFactory.avatar().toBuilder().small("Other Url").build()

        assertFalse(avatarA == avatarB)

        avatarB.toBuilder()
            .small(avatarA.small())
            .medium("Other Url")
            .build()

        assertFalse(avatarA == avatarB)
    }

    fun testEquals_whenSecondAvatarEquals_returnTrue() {
        val avatarA = AvatarFactory.avatar()
        val avatarB = Avatar.builder()
            .small(avatarA.small())
            .medium(avatarA.medium())
            .thumb(avatarA.thumb())
            .build()

        assertTrue(avatarA == avatarB)
        assertTrue(avatarA == avatarA)
    }
}