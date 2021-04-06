package com.kickstarter.libs.utils

import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.EventContextValues.VideoContextName.LENGTH
import com.kickstarter.libs.utils.EventContextValues.VideoContextName.POSITION
import com.kickstarter.libs.utils.RewardUtils.isItemized
import com.kickstarter.libs.utils.RewardUtils.isShippable
import com.kickstarter.libs.utils.RewardUtils.isTimeLimitedEnd
import com.kickstarter.libs.utils.extensions.addOnsCost
import com.kickstarter.libs.utils.extensions.bonus
import com.kickstarter.libs.utils.extensions.rewardCost
import com.kickstarter.libs.utils.extensions.shippingAmount
import com.kickstarter.libs.utils.extensions.totalAmount
import com.kickstarter.libs.utils.extensions.totalCountUnique
import com.kickstarter.libs.utils.extensions.totalQuantity
import com.kickstarter.models.Activity
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.Update
import com.kickstarter.models.User
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
            put("amount", checkoutData.amount())
            checkoutData.id()?.let { put("id", it.toString()) }
            put("payment_type", checkoutData.paymentType().rawValue().toLowerCase(Locale.getDefault()))
            put("amount_total_usd", checkoutData.totalAmount(project.staticUsdRate()))
            put("shipping_amount", checkoutData.shippingAmount())
            put("shipping_amount_usd", checkoutData.shippingAmount(project.staticUsdRate()))
            put("bonus_amount", checkoutData.bonus())
            put("bonus_amount_usd", checkoutData.bonus(project.staticUsdRate()))
            put("add_ons_count_total", pledgeData.totalQuantity())
            put("add_ons_count_unique", pledgeData.totalCountUnique())
            put("add_ons_minimum_usd", pledgeData.addOnsCost(project.staticUsdRate()))
        }

        return MapUtils.prefixKeys(properties, prefix)
    }

    @JvmOverloads
    fun checkoutProperties(checkoutData: CheckoutData, project: Project, addOns: List<Reward>?, prefix: String = "checkout_"): Map<String, Any> {
        val properties = HashMap<String, Any>().apply {
            put("amount", checkoutData.amount())
            put("checkout_amount", checkoutData.totalAmount(project.staticUsdRate()))
            checkoutData.id()?.let { put("id", it.toString()) }
            put("payment_type", checkoutData.paymentType().rawValue().toLowerCase(Locale.getDefault()))
            put("amount_total_usd", checkoutData.totalAmount(project.staticUsdRate()))
            put("shipping_amount", checkoutData.shippingAmount())
            put("shipping_amount_usd", checkoutData.shippingAmount(project.staticUsdRate()))
            put("bonus_amount", checkoutData.bonus())
            put("bonus_amount_usd", checkoutData.bonus(project.staticUsdRate()))
            put("add_ons_count_total", totalQuantity(addOns))
            put("add_ons_count_unique", totalCountUnique(addOns))
            put("add_ons_minimum_usd", addOnsCost(project.staticUsdRate(), addOns))
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
            put("everything", BooleanUtils.isTrue(params.isAllProjects))
            put("pwl", BooleanUtils.isTrue(params.staffPicks()))
            put("recommended", BooleanUtils.isTrue(params.recommended()))
            put("ref_tag", DiscoveryParamsUtils.refTag(params).tag())
            params.term()?.let { put("search_term", it) }
            put("social", BooleanUtils.isIntTrue(params.social()))
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
            put("watched", BooleanUtils.isIntTrue(params.starred()))

            val paramsCategory = params.category()
            paramsCategory?.let { category ->
                if (category.isRoot) {
                    putAll(categoryProperties(category))
                } else {
                    putAll(categoryProperties(category.root()))
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
            put("name", category.name().toString())
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
        properties["launched_projects_count"] = user.memberProjectsCount() ?: 0
        properties["created_projects_count"] = user.createdProjectsCount() ?: 0
        properties["facebook_connected"] = user.facebookConnected() ?: false
        properties["watched_projects_count"] = user.starredProjectsCount() ?: 0
        properties["uid"] = user.id().toString()
        properties["is_admin"] = user.isAdmin ?: false

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
    fun pledgeProperties(pledgeData: PledgeData, prefix: String = "checkout_reward_"): Map<String, Any> {
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
            put("minimum_usd", pledgeData.rewardCost(project.staticUsdRate()))
            reward.shippingPreference()?.let { put("shipping_preference", it) }
            reward.title()?.let { put("title", it) }
        }

        return MapUtils.prefixKeys(properties, prefix)
    }

    @JvmOverloads
    fun projectProperties(project: Project, loggedInUser: User?, prefix: String = "project_"): MutableMap<String, Any> {
        val properties = HashMap<String, Any>().apply {
            put("backers_count", project.backersCount())
            project.category()?.let { category ->
                if (category.isRoot) {
                    put("category", category.name())
                } else {
                    category.parent()?.let { parent ->
                        put("category", parent.name())
                    } ?: category.parentName()?.let {
                        put("category", it)
                    }
                    put("subcategory", category.name())
                }
            }
            project.commentsCount()?.let { put("comments_count", it) }
            project.prelaunchActivated()?.let { put("project_prelaunch_activated", it) }
            put("country", project.country())
            put("creator_uid", project.creator().id().toString())
            put("currency", project.currency())
            put("current_pledge_amount", project.pledged())
            put("current_amount_pledged_usd", project.pledged() * project.staticUsdRate())
            project.deadline()?.let { deadline ->
                put("deadline", deadline)
            }
            put("duration", ProjectUtils.timeInSecondsOfDuration(project).toFloat().roundToInt())
            put("goal", project.goal())
            put("goal_usd", project.goal() * project.staticUsdRate())
            put("has_video", project.video() != null)
            put("hours_remaining", ceil((ProjectUtils.timeInSecondsUntilDeadline(project) / 60.0f / 60.0f).toDouble()).toInt())
            put("is_repeat_creator", IntegerUtils.intValueOrZero(project.creator().createdProjectsCount()) >= 2)
            project.launchedAt()?.let { launchedAt ->
                put("launched_at", launchedAt)
            }
            project.location()?.let { location ->
                put("location", location.name())
            }
            put("name", project.name())
            put("percent_raised", project.percentageFunded() / 100.0f)
            put("pid", project.id().toString())
            put("prelaunch_activated", BooleanUtils.isTrue(project.prelaunchActivated()))
            project.rewards()?.let { rewards ->
                put("rewards_count", rewards.size)
            }
            put("state", project.state())
            put("static_usd_rate", project.staticUsdRate())
            project.updatesCount()?.let { put("updates_count", it) }
            put("user_is_project_creator", ProjectUtils.userIsCreator(project, loggedInUser))
            put("user_is_backer", project.isBacking)
            put("user_has_watched", project.isStarred)

            val hasAddOns = project.rewards()?.find {
                it.hasAddons()
            }
            put("has_add_ons", hasAddOns?.hasAddons() ?: false)
        }

        return MapUtils.prefixKeys(properties, prefix)
    }

    fun refTagProperties(intentRefTag: RefTag?, cookieRefTag: RefTag?) = HashMap<String, Any>().apply {
        intentRefTag?.tag()?.let { put("session_ref_tag", it) }
        cookieRefTag?.tag()?.let { put("session_referrer_credit", it) }
    }

    @JvmOverloads
    fun activityProperties(activity: Activity, loggedInUser: User?, prefix: String = "activity_"): Map<String, Any> {
        val props: HashMap<String, Any> = hashMapOf("category" to activity.category())

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
}
