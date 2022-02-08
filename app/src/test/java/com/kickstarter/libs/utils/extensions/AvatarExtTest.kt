package com.kickstarter.libs.utils.extensions

import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.models.extensions.replaceSmallImageWithMediumIfEmpty
import junit.framework.TestCase
import org.junit.Test

class AvatarExtTest : TestCase() {
    @Test
    fun testReplaceSmallImageWithMediumIfEmpty() {
        val avatarA = AvatarFactory.avatar()
        val avatarB = AvatarFactory.avatar().toBuilder().small("").build()

        assertEquals(avatarA.replaceSmallImageWithMediumIfEmpty(), avatarA.small())
        assertEquals(avatarB.replaceSmallImageWithMediumIfEmpty(), avatarB.medium())
    }
}
