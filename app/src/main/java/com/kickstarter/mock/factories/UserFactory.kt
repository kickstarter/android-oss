package com.kickstarter.mock.factories

import com.kickstarter.mock.factories.LocationFactory.germany
import com.kickstarter.mock.factories.LocationFactory.mexico
import com.kickstarter.mock.factories.LocationFactory.unitedStates
import com.kickstarter.models.User
import com.kickstarter.models.User.Companion.builder

object UserFactory {
    @JvmStatic
    fun user(): User {
        return builder()
            .avatar(AvatarFactory.avatar())
            .id(IdFactory.id().toLong())
            .isEmailVerified(true)
            .name("Some Name")
            .optedOutOfRecommendations(false)
            .location(unitedStates())
            .build()
    }

    fun userNotVerifiedEmail(): User {
        return builder()
            .avatar(AvatarFactory.avatar())
            .id(IdFactory.id().toLong())
            .isEmailVerified(false)
            .name("Some Name")
            .optedOutOfRecommendations(false)
            .location(unitedStates())
            .build()
    }

    @JvmStatic
    fun socialUser(): User {
        return user().toBuilder().social(true).build()
    }

    @JvmStatic
    fun userNeedPassword(): User {
        return user().toBuilder().needsPassword(true).build()
    }

    @JvmStatic
    fun collaborator(): User {
        return user()
            .toBuilder()
            .createdProjectsCount(0)
            .memberProjectsCount(10)
            .build()
    }

    @JvmStatic
    fun creator(): User {
        return user()
            .toBuilder()
            .createdProjectsCount(5)
            .memberProjectsCount(10)
            .build()
    }

    @JvmStatic
    fun germanUser(): User {
        return user()
            .toBuilder()
            .location(germany())
            .build()
    }

    fun canadianUser(): User {
        return user()
            .toBuilder()
            .location(germany())
            .build()
    }

    fun mexicanUser(): User {
        return user()
            .toBuilder()
            .location(mexico())
            .build()
    }

    @JvmStatic
    fun noRecommendations(): User {
        return user()
            .toBuilder()
            .optedOutOfRecommendations(true)
            .build()
    }

    fun allTraitsTrue(): User {
        return user().toBuilder()
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
    }
}
