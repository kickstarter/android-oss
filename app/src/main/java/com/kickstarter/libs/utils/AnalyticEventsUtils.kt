package com.kickstarter.libs.utils

import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.EventContextValues.VideoContextName.LENGTH
import com.kickstarter.libs.utils.EventContextValues.VideoContextName.POSITION
import com.kickstarter.libs.utils.RewardUtils.isItemized
import com.kickstarter.libs.utils.RewardUtils.isReward
import com.kickstarter.libs.utils.RewardUtils.isShippable
import com.kickstarter.libs.utils.RewardUtils.isTimeLimitedEnd
import com.kickstarter.libs.utils.extensions.addOnsCost
import com.kickstarter.libs.utils.extensions.bonus
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.refTag
import com.kickstarter.libs.utils.extensions.rewardCost
import com.kickstarter.libs.utils.extensions.round
import com.kickstarter.libs.utils.extensions.shippingAmount
import com.kickstarter.libs.utils.extensions.showLatePledgeFlow
import com.kickstarter.libs.utils.extensions.timeInDaysOfDuration
import com.kickstarter.libs.utils.extensions.timeInSecondsUntilDeadline
import com.kickstarter.libs.utils.extensions.totalAmount
import com.kickstarter.libs.utils.extensions.totalCountUnique
import com.kickstarter.libs.utils.extensions.totalQuantity
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.models.Activity
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.Update
import com.kickstarter.models.User
import com.kickstarter.models.extensions.getCreatedAndDraftProjectsCount
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

object AnalyticEventsUtils {

    @JvmOverloads
    fun checkoutProperties(checkoutData: CheckoutData, pledgeData: PledgeData, prefix: String = "checkout_"): Map<String, Any> {
        val project = pledgeData.projectData().project()
        val properties = HashMap<String, Any>().apply {
            put("amount", checkoutData.amount().round())
            checkoutData.id()?.let { put("id", it.toString()) }
            put(
                "payment_type",
                checkoutData.paymentType().rawValue.lowercase(Locale.getDefault())
            )
            put("amount_total_usd", checkoutData.totalAmount(project.staticUsdRate()).round())
            put("shipping_amount", checkoutData.shippingAmount())
            put("shipping_amount_usd", checkoutData.shippingAmount(project.staticUsdRate()).round())
            put("bonus_amount", checkoutData.bonus())
            put("bonus_amount_usd", checkoutData.bonus(project.staticUsdRate()).round())
            put("add_ons_count_total", pledgeData.totalQuantity())
            put("add_ons_count_unique", pledgeData.totalCountUnique())
            put("add_ons_minimum_usd", pledgeData.addOnsCost(project.staticUsdRate()).round())
        }

        return MapUtils.prefixKeys(properties, prefix)
    }

    @JvmOverloads
    fun checkoutProperties(checkoutData: CheckoutData, project: Project, addOns: List<Reward>?, prefix: String = "checkout_"): Map<String, Any> {
        val properties = HashMap<String, Any>().apply {
            put("amount", checkoutData.amount().round())
            checkoutData.id()?.let { put("id", it.toString()) }
            put(
                "payment_type",
                checkoutData.paymentType().rawValue.lowercase(Locale.getDefault())
            )
            put("amount_total_usd", checkoutData.totalAmount(project.staticUsdRate()).round())
            put("shipping_amount", checkoutData.shippingAmount())
            put("shipping_amount_usd", checkoutData.shippingAmount(project.staticUsdRate()).round())
            put("bonus_amount", checkoutData.bonus())
            put("bonus_amount_usd", checkoutData.bonus(project.staticUsdRate()).round())
            put("add_ons_count_total", totalQuantity(addOns))
            put("add_ons_count_unique", totalCountUnique(addOns))
            put("add_ons_minimum_usd", addOnsCost(project.staticUsdRate(), addOns).round())
        }

        return MapUtils.prefixKeys(properties, prefix)
    }

    fun checkoutDataProperties(checkoutData: CheckoutData, pledgeData: PledgeData, loggedInUser: User?): Map<String, Any> {
        val props = pledgeDataProperties(pledgeData, loggedInUser)
        props.putAll(checkoutProperties(checkoutData, pledgeData))
        return props
    }

    @JvmOverloads
    fun discoveryParamsProperties(params: DiscoveryParams, discoverSort: DiscoveryParams.Sort? = params.sort(), prefix: String = "discover_"): Map<String, Any> {
        val properties = HashMap<String, Any>().apply {
            put("everything", params.isAllProjects.isTrue() && params.recommended()?.isFalse() ?: true)
            put("pwl", params.staffPicks().isTrue())
            put("recommended", params.recommended()?.isTrue() ?: false)
            params.refTag()?.tag()?.let { put("ref_tag", it) }
            params.term()?.let { put("search_term", it) }
            put("social", params.social().isNonZero())
            put(
                "sort",
                discoverSort?.let {
                    when (it) {
                        DiscoveryParams.Sort.POPULAR -> "popular"
                        DiscoveryParams.Sort.ENDING_SOON -> "ending_soon"
                        else -> it.toString()
                    }
                } ?: ""
            )
            params.tagId()?.let { put("tag", it) }
            put("watched", params.starred().isNonZero())

            val paramsCategory = params.category()
            paramsCategory?.let { category ->
                if (category.isRoot) {
                    putAll(categoryProperties(category))
                } else {
                    category.root()?.let { putAll(categoryProperties(it)) }
                    putAll(subcategoryProperties(category))
                }
            }
        }
        return MapUtils.prefixKeys(properties, prefix)
    }

    fun subcategoryProperties(category: Category): Map<String, Any> {
        return categoryProperties(category, "subcategory_")
    }

    @JvmOverloads
    fun categoryProperties(category: Category, prefix: String = "category_"): Map<String, Any> {
        val properties = HashMap<String, Any>().apply {
            put("id", category.id().toString())
            put("name", category.analyticsName().toString())
        }
        return MapUtils.prefixKeys(properties, prefix)
    }

    @JvmOverloads
    fun locationProperties(location: Location, prefix: String = "location_"): Map<String, Any> {
        val properties = HashMap<String, Any>().apply {
            put("id", location.id().toString())
            put("name", location.name())
            put("displayable_name", location.displayableName())
            location.city()?.let { put("city", it) }
            location.state()?.let { put("state", it) }
            put("country", location.country())
            location.projectsCount()?.let { put("projects_count", it) }
        }

        return MapUtils.prefixKeys(properties, prefix)
    }

    @JvmOverloads
    fun userProperties(user: User, prefix: String = "user_"): Map<String, Any> {
        val properties = HashMap<String, Any>()
        properties["backed_projects_count"] = user.backedProjectsCount() ?: 0
        properties["launched_projects_count"] = user.createdProjectsCount() ?: 0
        properties["created_projects_count"] = user.getCreatedAndDraftProjectsCount()
        properties["facebook_connected"] = user.facebookConnected() ?: false
        properties["watched_projects_count"] = user.starredProjectsCount() ?: 0
        properties["uid"] = user.id().toString()
        properties["is_admin"] = user.isAdmin() ?: false

        return MapUtils.prefixKeys(properties, prefix)
    }

    fun pledgeDataProperties(pledgeData: PledgeData, loggedInUser: User?): MutableMap<String, Any> {
        val projectData = pledgeData.projectData()
        val props = projectProperties(projectData.project(), loggedInUser)
        props.putAll(pledgeProperties(pledgeData))
        props.putAll(refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        props["context_pledge_flow"] = pledgeData.pledgeFlowContext().trackingString
        return props
    }

    @JvmOverloads
    fun videoProperties(videoLength: Long, videoPosition: Long, prefix: String = "video_"): Map<String, Any> {

        val properties = HashMap<String, Any>().apply {
            put(LENGTH.contextName, videoLength)
            put(POSITION.contextName, videoPosition)
        }
        return MapUtils.prefixKeys(properties, prefix)
    }

    @JvmOverloads
    fun pledgeProperties(pledgeData: PledgeData, prefix: String = "checkout_"): Map<String, Any> {
        val reward = pledgeData.reward()
        val project = pledgeData.projectData().project()
        val properties = HashMap<String, Any>().apply {
            reward.estimatedDeliveryOn()?.let { deliveryDate ->
                put("estimated_delivery_on", deliveryDate)
            }
            put("has_items", isItemized(reward))
            put("id", reward.id().toString())
            put("is_limited_time", isTimeLimitedEnd(reward))
            put("is_limited_quantity", reward.limit() != null)
            put("minimum", reward.minimum())
            put("shipping_enabled", isShippable(reward))
            put("minimum_usd", pledgeData.rewardCost(project.staticUsdRate()).round())
            reward.shippingPreference()?.let { put("shipping_preference", it) }
            reward.title()?.let { put("title", it) }
        }

        val props = MapUtils.prefixKeys(properties, "reward_")

        props.apply {
            put("add_ons_count_total", pledgeData.totalQuantity())
            put("add_ons_count_unique", pledgeData.totalCountUnique())
            put("add_ons_minimum_usd", addOnsCost(project.staticUsdRate(), pledgeData.addOns()?.let { it as? List<Reward> } ?: emptyList()).round())
        }

        return MapUtils.prefixKeys(props, prefix)
    }

    @JvmOverloads
    fun projectProperties(project: Project, loggedInUser: User?, prefix: String = "project_"): MutableMap<String, Any> {
        val properties = HashMap<String, Any>().apply {
            put("backers_count", project.backersCount())
            project.category()?.let { category ->
                if (category.isRoot) {
                    put("category", category.analyticsName())
                } else {
                    category.parent()?.let { parent ->
                        put("category", parent.analyticsName())
                    } ?: category.parentName()?.let {
                        if (!this.containsKey("category")) this["category"] = it
                    }
                    put("subcategory", category.analyticsName())
                }
            }
            project.commentsCount()?.let { put("comments_count", it) }
            project.prelaunchActivated()?.let { put("project_prelaunch_activated", it) }
            put("country", project.country())
            put("creator_uid", project.creator().id().toString())
            put("currency", project.currency())
            put("current_pledge_amount", project.pledged())
            put("current_amount_pledged_usd", (project.pledged() * project.usdExchangeRate()).round())
            project.deadline()?.let { deadline ->
                put("deadline", deadline)
            }
            put("duration", project.timeInDaysOfDuration().toFloat().roundToInt())
            put("goal", project.goal())
            put("goal_usd", (project.goal() * project.usdExchangeRate()).round())
            put("has_video", project.video() != null)
            put("hours_remaining", ceil((project.timeInSecondsUntilDeadline() / 60.0f / 60.0f).toDouble()).toInt())
            put("is_repeat_creator", project.creator().createdProjectsCount().intValueOrZero() >= 2)
            project.launchedAt()?.let { launchedAt ->
                put("launched_at", launchedAt)
            }
            project.location()?.let { location ->
                put("location", location.name())
            }
            put("name", project.name())
            put("percent_raised", (project.percentageFunded()).toInt())
            put("pid", project.id().toString())
            put("prelaunch_activated", project.prelaunchActivated().isTrue())

            project.rewards()?.let { a ->
                val rewards = a.filter { isReward(it) }
                put("rewards_count", rewards.size)
            }
            put("state", if (!project.showLatePledgeFlow()) project.state() else "post_campaign")
            put("static_usd_rate", project.staticUsdRate())
            project.updatesCount()?.let { put("updates_count", it) }
            put("user_is_project_creator", project.userIsCreator(loggedInUser))
            put("user_is_backer", project.isBacking())
            put("user_has_watched", project.isStarred())

            val hasAddOns = project.rewards()?.find {
                it.hasAddons()
            }
            put("has_add_ons", hasAddOns?.hasAddons() ?: false)
            put("tags", project.tags()?.let { it.joinToString(", ") } ?: "")
            put("url", project.urls().web().project())
            put("project_post_campaign_enabled", project.showLatePledgeFlow())
            project.photo()?.full()?.let { put("image_url", it) }
        }

        return MapUtils.prefixKeys(properties, prefix)
    }

    fun refTagProperties(intentRefTag: RefTag?, cookieRefTag: RefTag?) = HashMap<String, Any>().apply {
        intentRefTag?.tag()?.let { put("session_ref_tag", it) }
        cookieRefTag?.tag()?.let { put("session_referrer_credit", it) }
    }

    @JvmOverloads
    fun activityProperties(activity: Activity, loggedInUser: User?, prefix: String = "activity_"): Map<String, Any> {
        val props = HashMap<String, Any>().apply {
            activity.category()?.let { put("category", it) }
        }

        val properties = MapUtils.prefixKeys(props, prefix)
        activity.project()?.let { project ->
            properties.putAll(projectProperties(project, loggedInUser))
            activity.update()?.let { update ->
                properties.putAll(updateProperties(project, update, loggedInUser))
            }
        }
        return properties
    }

    @JvmOverloads
    fun updateProperties(project: Project, update: Update, loggedInUser: User?, prefix: String = "update_"): Map<String, Any> {
        val props = HashMap<String, Any>().apply {
            update.commentsCount()?.let { put("comments_count", it) }
            update.hasLiked()?.let { put("has_liked", it) }
            put("id", update.id())
            update.likesCount()?.let { put("likes_count", it) }
            put("title", update.title())
            put("sequence", update.sequence())
            update.visible()?.let { put("visible", it) }
            update.publishedAt()?.let { put("published_at", it) }
        }
        val properties = MapUtils.prefixKeys(props, prefix)
        properties.putAll(projectProperties(project, loggedInUser))
        return properties
    }

    @JvmOverloads
    fun notificationProperties(ppoCards: List<PPOCard?>, totalCount: Int, prefix: String = "notification_count_"): Map<String, Any> {
        val props = HashMap<String, Int>().apply {
            put("address_locks_soon", 0)
            put("survey_available", 0)
            put("card_auth_required", 0)
            put("payment_failed", 0)
            put("total", totalCount)
        }

        for (card in ppoCards) {
            when (card?.viewType()) {
                PPOCardViewType.FIX_PAYMENT -> props["payment_failed"] = (props["payment_failed"] ?: 0).plus(1)
                PPOCardViewType.AUTHENTICATE_CARD -> props["card_auth_required"] = (props["card_auth_required"] ?: 0).plus(1)
                PPOCardViewType.OPEN_SURVEY -> props["survey_available"] = (props["survey_available"] ?: 0).plus(1)
                PPOCardViewType.CONFIRM_ADDRESS -> props["address_locks_soon"] = (props["address_locks_soon"] ?: 0).plus(1)
                else -> {}
            }
        }

        val properties = MapUtils.prefixKeys(props, prefix)
        return properties
    }
}
