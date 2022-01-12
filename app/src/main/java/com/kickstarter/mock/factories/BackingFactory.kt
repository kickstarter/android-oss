package com.kickstarter.mock.factories

import com.kickstarter.mock.factories.PaymentSourceFactory.Companion.visa
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.models.Backing
import com.kickstarter.models.Backing.Companion.builder
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.User
import org.joda.time.DateTime
import kotlin.jvm.JvmOverloads

object BackingFactory {

    @JvmStatic
    @JvmOverloads
    fun backing(backer: User): Backing {
        return backing(project(), backer, RewardFactory.reward())
    }

    @JvmStatic
    @JvmOverloads
    fun backing(reward: Reward): Backing {
        return backing(project(), user(), reward)
    }

    @JvmStatic
    @JvmOverloads
    fun backing(
        project: Project = project(),
        backer: User = user(),
        reward: Reward = RewardFactory.reward()
    ): Backing {
        return builder()
            .amount(10.0)
            .backer(backer)
            .backerId(backer.id())
            .backerName("Pikachu")
            .backerUrl("www.avatars.com/medium.jpg")
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .paymentSource(visa())
            .pledgedAt(DateTime.now())
            .project(project)
            .projectId(project.id())
            .reward(reward)
            .rewardId(reward.id())
            .sequence(1)
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build()
    }

    @JvmStatic
    @JvmOverloads
    fun backingNull(): Backing {
        return builder()
            .amount(0.0)
            .backer(null)
            .backerId(user().id())
            .backerName(null)
            .backerUrl(null)
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .paymentSource(null)
            .pledgedAt(DateTime.now())
            .project(null)
            .projectId(ProjectFactory.project().id())
            .reward(null)
            .rewardId(null)
            .sequence(1)
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build()
    }

    @JvmStatic
    @JvmOverloads
    fun backing(status: String): Backing {
        return backing()
            .toBuilder()
            .status(status)
            .build()
    }
}
