package com.kickstarter.mock.factories

import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.Urls
import com.kickstarter.models.Web
import com.kickstarter.type.CreditCardPaymentType
import com.kickstarter.type.CreditCardTypes
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

object ProjectFactory {
    @JvmStatic
    fun project(): Project {
        val creator = UserFactory.creator()
        val slug = "slug-1"
        val projectUrl = "https://www.kickstarter.com/projects/" + creator.id() + "/" + slug
        val web = Web.builder()
            .project(projectUrl)
            .rewards("$projectUrl/rewards")
            .updates("$projectUrl/posts")
            .build()
        return Project.builder()
            .availableCardTypes(
                listOf(
                    CreditCardTypes.AMEX.rawValue,
                    CreditCardTypes.DINERS.rawValue,
                    CreditCardTypes.DISCOVER.rawValue,
                    CreditCardTypes.JCB.rawValue,
                    CreditCardTypes.MASTERCARD.rawValue,
                    CreditCardTypes.UNIONPAY.rawValue,
                    CreditCardTypes.VISA.rawValue
                )
            )
            .backersCount(100)
            .blurb("Some blurb")
            .category(CategoryFactory.category())
            .creator(UserFactory.creator())
            .country("US")
            .createdAt(DateTime.now(DateTimeZone.UTC))
            .currency("USD")
            .currencySymbol("$")
            .currentCurrency("USD")
            .currencyTrailingCode(true)
            .fxRate(1.0f)
            .goal(100.0)
            .prelaunchActivated(false)
            .watchesCount(100)
            .id(IdFactory.id().toLong())
            .location(LocationFactory.unitedStates())
            .name("Some Name")
            .pledged(50.0)
            .photo(PhotoFactory.photo())
            .rewards(listOf(RewardFactory.noReward(), RewardFactory.reward()))
            .staffPick(false)
            .state(Project.STATE_LIVE)
            .staticUsdRate(1.0f)
            .usdExchangeRate(1.0f)
            .sendMetaCapiEvents(false)
            .slug(slug)
            .projectFaqs(ProjectFaqFactory.getFaqs())
            .envCommitments(ProjectEnvironmentalCommitmentFactory.getEnvironmentalCommitments())
            .updatedAt(DateTime.now())
            .urls(Urls.builder().web(web).build())
            .video(VideoFactory.video())
            .sendThirdPartyEvents(true)
            .launchedAt(DateTime(DateTimeZone.UTC).minusDays(10))
            .deadline(DateTime(DateTimeZone.UTC).plusDays(10))
            .risks("Risks and challenges")
            .build()
    }

    @JvmStatic
    fun backedProjectWithError(): Project {
        val project = project()
        val reward = RewardFactory.reward()
        val backing = Backing.builder()
            .amount(10.0)
            .backerId(IdFactory.id().toLong())
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .paymentSource(PaymentSourceFactory.visa())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_ERRORED)
            .build()
        return project
            .toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun britishProject(): Project {
        return project()
            .toBuilder()
            .country("GB")
            .currency("GBP")
            .currencySymbol("£")
            .build()
    }

    @JvmStatic
    fun backedProject(): Project {
        val project = project()
        val reward = RewardFactory.reward()
        val backing = Backing.builder()
            .amount(10.0)
            .backerId(IdFactory.id().toLong())
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .paymentSource(PaymentSourceFactory.visa())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build()
        return project
            .toBuilder()
            .availableCardTypes(listOf(PaymentSourceFactory.visa().type() ?: CreditCardPaymentType.CREDIT_CARD.rawValue))
            .canComment(true)
            .backing(backing)
            .isBacking(true)
            .build()
    }
    @JvmStatic
    fun backedProjectWithPlotSelected(): Project {
        val project = project().toBuilder().isPledgeOverTimeAllowed(true).build()
        val reward = RewardFactory.reward()
        val backing = Backing.builder()
            .amount(10.0)
            .backerId(IdFactory.id().toLong())
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .incremental(true)
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .paymentSource(PaymentSourceFactory.visa())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build()
        return project
            .toBuilder()
            .availableCardTypes(listOf(PaymentSourceFactory.visa().type() ?: CreditCardPaymentType.CREDIT_CARD.rawValue))
            .canComment(true)
            .backing(backing)
            .isBacking(true)
            .build()
    }
    @JvmStatic
    fun backedProjectWithRewardAndAddOnsLimitReached(): Project {
        val project = project()
        val reward = RewardFactory.reward().toBuilder().hasAddons(true).limit(10).build()
        val add1 = RewardFactory.addOn()
            .toBuilder()
            .remaining(0)
            .limit(0)
            .quantity(1)
            .build()
        val addOns: MutableList<Reward> = ArrayList()
        addOns.add(add1)
        val backing = Backing.builder()
            .amount(10.0)
            .backerId(IdFactory.id().toLong())
            .addOns(addOns)
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .paymentSource(PaymentSourceFactory.visa())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build()
        return project
            .toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProjectWithAddOns(): Project {
        val project = project()
        val reward = RewardFactory.reward().toBuilder().hasAddons(true).build()
        val add1 = RewardFactory.addOn()
        val addOns: MutableList<Reward> = ArrayList()
        addOns.add(add1)
        addOns.add(add1)
        val backing = Backing.builder()
            .amount(10.0)
            .backerId(IdFactory.id().toLong())
            .addOns(addOns)
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .paymentSource(PaymentSourceFactory.visa())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build()
        return project
            .toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProjectRewardAvailableAddOnsNotBackedAddOns(): Project {
        val project = project()
        val reward = RewardFactory.reward().toBuilder().hasAddons(true).isAvailable(true).build()
        val backing = Backing.builder()
            .amount(10.0)
            .backerId(IdFactory.id().toLong())
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .paymentSource(PaymentSourceFactory.visa())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build()
        return project
            .toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun projectWithAddOns(): Project {
        val rwWithAddOn =
            RewardFactory.reward().toBuilder().hasAddons(true).build()
        val rw =
            RewardFactory.reward().toBuilder().hasAddons(false).build()
        return project().toBuilder().rewards(listOf(rw, rwWithAddOn)).build()
    }

    @JvmStatic
    fun backedSuccessfulProject(): Project {
        val project = successfulProject()
        val reward = RewardFactory.reward()
        val backing = Backing.builder()
            .amount(10.0)
            .backerId(IdFactory.id().toLong())
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build()
        return project
            .toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    fun backedProjectWithRewardLimited(): Project {
        val project = project()
        val reward = RewardFactory.limited()
        val backing = Backing.builder()
            .amount(10.0)
            .backerId(IdFactory.id().toLong())
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build()
        return project
            .toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProjectWithRewardLimitReached(): Project {
        val project = project()
        val reward = RewardFactory.limitReached()
            .toBuilder()
            .hasAddons(true)
            .build()
        val backing = Backing.builder()
            .amount(10.0)
            .backerId(IdFactory.id().toLong())
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .sequence(1)
            .reward(reward)
            .rewardId(reward.id())
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build()
        return project
            .toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun backedProjectWithNoReward(): Project {
        val project = project()
        val reward = RewardFactory.noReward()
        val backing = Backing.builder()
            .amount(10.0)
            .backerId(IdFactory.id().toLong())
            .cancelable(true)
            .id(IdFactory.id().toLong())
            .sequence(1)
            .reward(reward)
            .rewardId(null)
            .pledgedAt(DateTime.now())
            .projectId(project.id())
            .shippingAmount(0.0f)
            .status(Backing.STATUS_PLEDGED)
            .build()
        return project
            .toBuilder()
            .backing(backing)
            .isBacking(true)
            .build()
    }

    @JvmStatic
    fun halfWayProject(): Project {
        return project()
            .toBuilder()
            .name("halfwayProject")
            .goal(100.0)
            .pledged(50.0)
            .build()
    }

    @JvmStatic
    fun allTheWayProject(): Project {
        return project()
            .toBuilder()
            .name("allTheWayProject")
            .goal(100.0)
            .pledged(100.0)
            .build()
    }

    @JvmStatic
    fun doubledGoalProject(): Project {
        return project()
            .toBuilder()
            .name("doubledGoalProject")
            .goal(100.0)
            .pledged(200.0)
            .build()
    }

    @JvmStatic
    fun failedProject(): Project {
        return project()
            .toBuilder()
            .name("failedProject")
            .state(Project.STATE_FAILED)
            .build()
    }

    @JvmStatic
    fun caProject(): Project {
        return project()
            .toBuilder()
            .availableCardTypes(
                listOf(
                    CreditCardTypes.AMEX.rawValue,
                    CreditCardTypes.MASTERCARD.rawValue,
                    CreditCardTypes.VISA.rawValue
                )
            )
            .name("caProject")
            .country("CA")
            .currentCurrency("CAD")
            .currencySymbol("$")
            .currency("CAD")
            .staticUsdRate(0.75f)
            .usdExchangeRate(0.75f)
            .fxRate(0.75f)
            .build()
    }

    @JvmStatic
    fun mxCurrencyCAProject(): Project {
        return project()
            .toBuilder()
            .availableCardTypes(
                listOf(
                    CreditCardTypes.AMEX.rawValue,
                    CreditCardTypes.MASTERCARD.rawValue,
                    CreditCardTypes.VISA.rawValue
                )
            )
            .name("mxCurrencyCAProject")
            .country("CA")
            .currentCurrency("MXN")
            .currencySymbol("$")
            .currency("CAD")
            .staticUsdRate(0.75f)
            .usdExchangeRate(0.75f)
            .fxRate(.75f)
            .build()
    }

    fun mxProject(): Project {
        return project()
            .toBuilder()
            .availableCardTypes(
                listOf(
                    CreditCardTypes.AMEX.rawValue,
                    CreditCardTypes.MASTERCARD.rawValue,
                    CreditCardTypes.VISA.rawValue
                )
            )
            .name("mxProject")
            .country("MX")
            .currentCurrency("MXN")
            .currencySymbol("$")
            .currency("MXN")
            .location(LocationFactory.mexico())
            .staticUsdRate(0.75f)
            .usdExchangeRate(0.75f)
            .fxRate(0.75f)
            .build()
    }

    @JvmStatic
    fun ukProject(): Project {
        return project()
            .toBuilder()
            .availableCardTypes(
                listOf(
                    CreditCardTypes.AMEX.rawValue,
                    CreditCardTypes.MASTERCARD.rawValue,
                    CreditCardTypes.VISA.rawValue
                )
            )
            .name("ukProject")
            .country("UK")
            .currentCurrency("GBP")
            .currencySymbol("£")
            .currency("GBP")
            .staticUsdRate(1.5f)
            .usdExchangeRate(0.75f)
            .fxRate(1.5f)
            .build()
    }

    @JvmStatic
    fun almostCompletedProject(): Project {
        return project()
            .toBuilder()
            .name("almostCompleteProject")
            .deadline(DateTime().plusDays(1))
            .build()
    }

    @JvmStatic
    fun successfulProject(): Project {
        return project()
            .toBuilder()
            .name("successfulProject")
            .deadline(DateTime().minus(2))
            .state(Project.STATE_SUCCESSFUL)
            .build()
    }

    @JvmStatic
    fun featured(): Project {
        return project()
            .toBuilder()
            .name("featuredProject")
            .featuredAt(DateTime())
            .build()
    }

    fun saved(): Project {
        return project()
            .toBuilder()
            .name("savedProject")
            .isStarred(true)
            .build()
    }

    @JvmStatic
    fun staffPick(): Project {
        return project()
            .toBuilder()
            .name("staffPickProject")
            .staffPick(true)
            .build()
    }

    fun prelaunchProject(projectUrl: String): Project {
        val web = Web.builder()
            .project(projectUrl)
            .rewards("$projectUrl/rewards")
            .updates("$projectUrl/posts")
            .build()

        return project()
            .toBuilder()
            .launchedAt(DateTime(0))
            .displayPrelaunch(true)
            .urls(Urls.builder().web(web).build())
            .build()
    }

    fun initialProject(): Project {
        return project()
            .toBuilder()
            .rewards(null)
            .build()
    }
}
