package com.kickstarter.services.transformers

import com.google.android.gms.common.util.Base64Utils
import com.kickstarter.libs.Permission
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Avatar
import com.kickstarter.models.Backing
import com.kickstarter.models.Category
import com.kickstarter.models.Comment
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.models.Item
import com.kickstarter.models.Location
import com.kickstarter.models.PaymentSource
import com.kickstarter.models.Photo
import com.kickstarter.models.Project
import com.kickstarter.models.ProjectFaq
import com.kickstarter.models.Relay
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.Update
import com.kickstarter.models.Urls
import com.kickstarter.models.User
import com.kickstarter.models.UserPrivacy
import com.kickstarter.models.Video
import com.kickstarter.models.Web
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import fragment.FullProject
import fragment.ProjectCard
import org.jetbrains.annotations.Nullable
import org.joda.time.DateTime
import type.CollaboratorPermission
import type.CreditCardPaymentType
import type.CurrencyCode
import type.RewardType
import type.ShippingPreference
import java.nio.charset.Charset
import kotlin.math.absoluteValue

/**
 * Set of package level functions that will be used to transform the GraphQL data structures into
 * Kickstarter Data Models.
 */

fun decodeRelayId(encodedRelayId: String?): Long? {
    return try {
        encodedRelayId?.let {
            String(Base64Utils.decode(it), Charset.defaultCharset())
                .replaceBeforeLast("-", "", "")
                .toLong()
                .absoluteValue
        }
    } catch (e: Exception) {
        null
    }
}

fun <T : Relay> encodeRelayId(relay: T): String {
    val classSimpleName = relay.javaClass.simpleName.replaceFirst("AutoParcel_", "")
    val id = relay.id()
    return Base64Utils.encodeUrlSafe(("$classSimpleName-$id").toByteArray(Charset.defaultCharset()))
}

/**
 * Transform the ProjectFaq GraphQL data structure into our own ProjectFaq data model
 * @param fragment.Faq faq
 * @return ProjectFaq
 */
fun projectFaqTransformer(faq: fragment.Faq): ProjectFaq {
    val id = decodeRelayId(faq.id()) ?: -1
    val answer = faq.answer()
    val createdAt = faq.createdAt()
    val question = faq.question()

    return ProjectFaq.builder()
        .id(id)
        .answer(answer)
        .createdAt(createdAt)
        .question(question)
        .build()
}

/**
 * Transform the EnvironmentalCommitment GraphQL data structure into our own EnvironmentalCommitment data model
 * @param fragment.EnvironmentalCommitment
 * @return EnvironmentalCommitment
 */
fun environmentalCommitmentTransformer(envCommit: fragment.EnvironmentalCommitment): EnvironmentalCommitment {
    val id = decodeRelayId(envCommit.id()) ?: -1
    val category = envCommit.commitmentCategory().name
    val description = envCommit.description()

    return EnvironmentalCommitment.builder()
        .id(id = id)
        .category(category = category)
        .description(description = description)
        .build()
}

/**
 * Transform the Reward GraphQL data structure into our own Reward data model
 * @param fragment.reward rewardGr
 * @return Reward
 */
fun rewardTransformer(
    rewardGr: fragment.Reward,
    shippingRulesExpanded: List<fragment.ShippingRule> = emptyList(),
    allowedAddons: Boolean = false,
    rewardItems: List<RewardsItem> = emptyList(),
    addOnItems: List<RewardsItem> = emptyList()
): Reward {
    val amount = rewardGr.amount().fragments().amount().amount()?.toDouble() ?: 0.0
    val convertedAmount =
        rewardGr.convertedAmount().fragments().amount().amount()?.toDouble() ?: 0.0
    val desc = rewardGr.description()
    val title = rewardGr.name()
    val estimatedDelivery = rewardGr.estimatedDeliveryOn()?.let { DateTime(it) }
    val remaining = rewardGr.remainingQuantity()
    val endsAt = rewardGr.endsAt()?.let { DateTime(it) }
    val startsAt = rewardGr.startsAt()?.let { DateTime(it) }
    val rewardId = decodeRelayId(rewardGr.id()) ?: -1
    val available = rewardGr.available()
    val isAddOn = rewardGr.rewardType() == RewardType.ADDON
    val isReward = rewardGr.rewardType() == RewardType.BASE
    val backersCount = rewardGr.backersCount()
    val shippingPreference = when (rewardGr.shippingPreference()) {
        ShippingPreference.NONE -> Reward.ShippingPreference.NONE
        ShippingPreference.RESTRICTED -> Reward.ShippingPreference.RESTRICTED
        ShippingPreference.UNRESTRICTED -> Reward.ShippingPreference.UNRESTRICTED
        ShippingPreference.LOCAL -> Reward.ShippingPreference.LOCAL
        else -> Reward.ShippingPreference.UNKNOWN
    }

    val limit = if (isAddOn) chooseLimit(rewardGr.limit(), rewardGr.limitPerBacker())
    else rewardGr.limit()

    val shippingRules = shippingRulesExpanded.map {
        shippingRuleTransformer(it)
    }

    val localReceiptLocation = locationTransformer(rewardGr.localReceiptLocation()?.fragments()?.location())

    return Reward.builder()
        .title(title)
        .convertedMinimum(convertedAmount)
        .minimum(amount)
        .limit(limit)
        .remaining(remaining)
        .endsAt(endsAt)
        .startsAt(startsAt)
        .description(desc)
        .estimatedDeliveryOn(estimatedDelivery)
        .isAddOn(isAddOn)
        .addOnsItems(addOnItems)
        .hasAddons(allowedAddons)
        .rewardsItems(rewardItems)
        .id(rewardId)
        .shippingPreference(shippingPreference.name.lowercase())
        .shippingPreferenceType(shippingPreference)
        .shippingType(shippingPreference.name.lowercase())
        .shippingRules(shippingRules)
        .isAvailable(available)
        .backersCount(backersCount)
        .localReceiptLocation(localReceiptLocation)
        .build()
}

/**
 * Choose the available limit being the smallest one, we can have limit by backer available just in add-ons
 * or limit by reward, available in V1 and Graphql and for both add-ons and Rewards
 * @return limit
 */
private fun chooseLimit(limitReward: Int?, limitPerBacker: Int?): Int {
    var limit = limitReward?.let { it } ?: -1
    var limitBacker = limitPerBacker?.let { it } ?: -1

    if (limit < 0) limit = limitBacker
    if (limitBacker < 0) limitBacker = limit

    return when (limit <= limitBacker) {
        true -> limit
        else -> limitBacker
    }
}

/**
 * Transform the Reward.Items GraphQL data structure into our own RewardsItems data model
 * @param fragment.Reward.items
 * @return List<RewardItem>
 */
fun complexRewardItemsTransformer(items: fragment.RewardItems?): List<RewardsItem> {
    val rewardItems = items?.edges()?.map { edge ->
        val quantity = edge.quantity()
        val description = edge.node()?.name()
        val id = decodeRelayId(edge.node()?.id()) ?: -1
        val name = edge.node()?.name() ?: ""

        val item = Item.builder()
            .name(name)
            .description(description)
            .id(id)
            .build()

        return@map RewardsItem.builder()
            .id(id)
            .itemId(item.id())
            .item(item)
            .rewardId(0) // - Discrepancy between V1 and Graph, the Graph object do not have the rewardID
            .quantity(quantity)
            .build()
    } ?: emptyList<RewardsItem>()
    return rewardItems.toList()
}

/**
 * Transform the Project GraphQL data structure into our own Project data model
 * @param fragment.FullProject projectFragment
 * @return Project
 */
fun projectTransformer(projectFragment: FullProject?): Project {
    val availableCards = projectFragment?.availableCardTypes() ?: emptyList()
    val backersCount = projectFragment?.backersCount() ?: 0
    val blurb = projectFragment?.description() ?: ""
    val backing = if (projectFragment?.backing()?.fragments()?.backing() != null) {
        backingTransformer(projectFragment.backing()?.fragments()?.backing())
    } else null
    val category = if (projectFragment?.category()?.fragments()?.category() != null) {
        categoryTransformer(projectFragment.category()?.fragments()?.category())
    } else null
    val commentsCount = projectFragment?.commentsCount() ?: 0
    val country = projectFragment?.country()?.fragments()?.country()?.name() ?: ""
    val createdAt = projectFragment?.createdAt()
    val creator = userTransformer(projectFragment?.creator()?.fragments()?.user())
    val currency = projectFragment?.currency()?.name ?: ""
    val currencySymbol = projectFragment?.goal()?.fragments()?.amount()?.symbol()
    val prelaunchActivated = projectFragment?.prelaunchActivated()
    val sendMetaCapiEvents = projectFragment?.sendMetaCapiEvents()
    val sendThirdPartyEvents = projectFragment?.sendThirdPartyEvents()
    val featuredAt = projectFragment?.projectOfTheDayAt()
    val friends =
        projectFragment?.friends()?.nodes()?.map { userTransformer(it.fragments().user()) }
            ?: emptyList<User>()
    val fxRate = projectFragment?.fxRate()?.toFloat()
    val deadline = projectFragment?.deadlineAt()
    val goal = projectFragment?.goal()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0
    val id = decodeRelayId(projectFragment?.id()) ?: -1
    val isBacking = projectFragment?.backing()?.fragments()?.backing()?.let { true } ?: false
    val isStarred = projectFragment?.isWatched ?: false
    val launchedAt = projectFragment?.launchedAt()
    val location = locationTransformer(projectFragment?.location()?.fragments()?.location())
    val name = projectFragment?.name()
    val permission = projectFragment?.collaboratorPermissions()?.map {
        when (it) {
            CollaboratorPermission.COMMENT -> Permission.COMMENT
            CollaboratorPermission.EDIT_FAQ -> Permission.EDIT_FAQ
            CollaboratorPermission.EDIT_PROJECT -> Permission.EDIT_PROJECT
            CollaboratorPermission.FULFILLMENT -> Permission.FULFILLMENT
            CollaboratorPermission.POST -> Permission.POST
            CollaboratorPermission.VIEW_PLEDGES -> Permission.VIEW_PLEDGES
            else -> Permission.UNKNOWN
        }
    }
    val pledged = projectFragment?.pledged()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0
    val photoUrl = projectFragment?.fragments()?.full()?.image()?.url()
    val photo = getPhoto(photoUrl)
    val tags = mutableListOf<String>()
    projectFragment?.fragments()?.tagsCreative()?.tags()?.map { tags.add(it.id()) }
    projectFragment?.fragments()?.tagsDiscovery()?.tags()?.map { tags.add(it.id()) }

    val minPledge = projectFragment?.minPledge()?.toDouble() ?: 1.0
    val rewards =
        projectFragment?.rewards()?.nodes()?.map {
            rewardTransformer(
                it.fragments().reward(),
                allowedAddons = it.allowedAddons().pageInfo().startCursor()?.isNotEmpty() ?: false,
                rewardItems = complexRewardItemsTransformer(it.items()?.fragments()?.rewardItems())
            )
        }

    // - GraphQL does not provide the Reward no reward, we need to add it first
    val modifiedRewards = rewards?.toMutableList()
    modifiedRewards?.add(0, RewardFactory.noReward().toBuilder().minimum(minPledge).build())
    modifiedRewards?.toList()

    val slug = projectFragment?.slug()
    val staffPicked = projectFragment?.isProjectWeLove ?: false
    val state = projectFragment?.state()?.name?.lowercase()
    val stateChangedAt = projectFragment?.stateChangedAt()
    val staticUSDRate = projectFragment?.usdExchangeRate()?.toFloat() ?: 1f
    val usdExchangeRate = projectFragment?.usdExchangeRate()?.toFloat() ?: 1f
    val updatedAt = projectFragment?.posts()?.fragments()?.updates()?.nodes()?.let {
        if (it.isNotEmpty()) return@let it.first()?.updatedAt()
        else null
    }
    val updatesCount = projectFragment?.posts()?.fragments()?.updates()?.totalCount()
    val url = projectFragment?.url()
    val urlsWeb = Web.builder()
        .project(url)
        .rewards("$url/rewards")
        .build()
    val urls = Urls.builder().web(urlsWeb).build()
    val video = if (projectFragment?.video()?.fragments()?.video() != null) {
        videoTransformer(projectFragment.video()?.fragments()?.video())
    } else null
    val displayPrelaunch = (projectFragment?.isLaunched ?: false).negate()
    val faqs = projectFragment?.faqs()?.nodes()?.map { node ->
        projectFaqTransformer(node.fragments().faq())
    } ?: emptyList()
    val eCommitment = projectFragment?.environmentalCommitments()?.map {
        environmentalCommitmentTransformer(it.fragments().environmentalCommitment())
    } ?: emptyList()
    val risks = projectFragment?.risks()
    val story = projectFragment?.story()?.toString() ?: ""
    val userCanComment = projectFragment?.canComment() ?: false
    val isFlagged = projectFragment?.flagging()?.kind()?.let { true } ?: false
    val watchesCount = projectFragment?.watchesCount() ?: 0

    return Project.builder()
        .availableCardTypes(availableCards.map { it.name })
        .backersCount(backersCount)
        .blurb(blurb)
        .canComment(userCanComment)
        .backing(backing)
        .category(category)
        .commentsCount(commentsCount)
        .country(country)
        .createdAt(createdAt)
        .creator(creator)
        .currency(currency)
        .currencySymbol(currencySymbol)
        .currentCurrency(currency) // - selected currency can be fetched form the User/Configuration Object
        .currencyTrailingCode(false) // - This field is available on V1 Configuration Object
        .displayPrelaunch(displayPrelaunch)
        .featuredAt(featuredAt)
        .friends(friends)
        .fxRate(fxRate)
        .deadline(deadline)
        .goal(goal)
        .id(id)
        .isBacking(isBacking)
        .isStarred(isStarred)
        .lastUpdatePublishedAt(updatedAt)
        .launchedAt(launchedAt)
        .location(location)
        .name(name)
        .permissions(permission)
        .pledged(pledged)
        .photo(photo) // - now we get the full size for field from GraphQL, but V1 provided several image sizes
        .prelaunchActivated(prelaunchActivated)
        .sendMetaCapiEvents(sendMetaCapiEvents)
        .sendThirdPartyEvents(sendThirdPartyEvents)
        .tags(tags)
        .rewards(modifiedRewards)
        .slug(slug)
        .staffPick(staffPicked)
        .state(state)
        .stateChangedAt(stateChangedAt)
        .staticUsdRate(staticUSDRate)
        .usdExchangeRate(usdExchangeRate)
        .updatedAt(updatedAt)
        // .unreadMessagesCount() unread messages can be fetched form the User Object
        // .unseenActivityCount() unseen activity can be fetched form the User Object
        .updatesCount(updatesCount)
        .urls(urls)
        .video(video)
        .projectFaqs(faqs)
        .envCommitments(eCommitment)
        .risks(risks)
        .story(story)
        .isFlagged(isFlagged)
        .watchesCount(watchesCount)
        .build()
}

/**
 * Transform the Category GraphQL data structure into our own Categroy data model
 * @param fragment.Category category
 * @return Project
 */
fun categoryTransformer(categoryFragment: fragment.Category?): Category {
    val analyticsName = categoryFragment?.analyticsName() ?: ""
    val name = categoryFragment?.name() ?: ""
    val id = decodeRelayId(categoryFragment?.id()) ?: -1
    val slug = categoryFragment?.slug()
    val parentId = decodeRelayId(categoryFragment?.parentCategory()?.id()) ?: 0
    val parentName = categoryFragment?.parentCategory()?.name()
    val parentSlug = categoryFragment?.parentCategory()?.slug()
    val parentAnalyticName = categoryFragment?.parentCategory()?.analyticsName() ?: ""

    val parentCategory = if (parentId > 0) {
        Category.builder()
            .slug(parentSlug)
            .analyticsName(parentAnalyticName)
            .id(parentId)
            .name(parentName)
            .build()
    } else null

    return Category.builder()
        .analyticsName(analyticsName)
        .id(id)
        .name(name)
        .slug(slug)
        .parent(parentCategory)
        .parentId(parentId)
        .parentName(parentName)
        .build()
}

/**
 * Transform the User GraphQL data structure into our own User data model
 * @param fragment.User user
 * @return User
 */
fun userTransformer(user: fragment.User?): User {
    val id = decodeRelayId(user?.id()) ?: -1
    val name = user?.name()
    val avatar = Avatar.builder()
        .medium(user?.imageUrl())
        .build()
    val chosenCurrency = user?.chosenCurrency() ?: CurrencyCode.USD.rawValue()

    return User.builder()
        .id(id)
        .name(name)
        .avatar(avatar)
        .chosenCurrency(chosenCurrency)
        .build()
}

/**
 * Transform the UserPrivacy GraphQL data structure into our own UserPrivacy data model
 * @param UserPrivacyQuery.Me userPrivacy
 * @return UserPrivacy
 */
fun userPrivacyTransformer(userPrivacy: UserPrivacyQuery.Me): UserPrivacy {
    val defaultCurrency = CurrencyCode.USD.rawValue()
    return UserPrivacy(
        name = userPrivacy.name(),
        email = userPrivacy.email() ?: "",
        hasPassword = userPrivacy.hasPassword() ?: false,
        isCreator = userPrivacy.isCreator ?: false,
        isDeliverable = userPrivacy.isDeliverable ?: false,
        isEmailVerified = userPrivacy.isEmailVerified ?: false,
        chosenCurrency = userPrivacy.chosenCurrency() ?: defaultCurrency)
}

/**
 * Transform the Project GraphQL data structure into our own Project data model
 * @param fragment.ProjectCard projectFragment
 * @return Project
 */
fun projectTransformer(projectFragment: ProjectCard?): Project {
    val backersCount = projectFragment?.backersCount() ?: 0
    val blurb = projectFragment?.description() ?: ""
    val category = if (projectFragment?.category()?.fragments()?.category() != null) {
        categoryTransformer(projectFragment.category()?.fragments()?.category())
    } else null
    val country = projectFragment?.country()?.fragments()?.country()?.name() ?: ""
    val createdAt = projectFragment?.createdAt()
    val creator = userTransformer(projectFragment?.creator()?.fragments()?.user())
    val currencySymbol = projectFragment?.goal()?.fragments()?.amount()?.symbol()
    val prelaunchActivated = projectFragment?.prelaunchActivated()
    val featuredAt = projectFragment?.projectOfTheDayAt()
    val friends =
        projectFragment?.friends()?.nodes()?.map { userTransformer(it.fragments().user()) }
            ?: emptyList()
    val pledged = projectFragment?.pledged()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0
    val fxRate = projectFragment?.fxRate()?.toFloat()
    val deadline = projectFragment?.deadlineAt()
    val goal = projectFragment?.goal()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0
    val id = decodeRelayId(projectFragment?.id()) ?: -1
    val isBacking = projectFragment?.backing()?.id()?.let { true } ?: false
    val isStarred = projectFragment?.isWatched ?: false
    val launchedAt = projectFragment?.launchedAt()
    val location = locationTransformer(projectFragment?.location()?.fragments()?.location())
    val name = projectFragment?.name()
    val photoUrl = projectFragment?.fragments()?.full()?.image()?.url()
    val photo = getPhoto(photoUrl)
    val slug = projectFragment?.slug()
    val staffPicked = projectFragment?.isProjectWeLove ?: false
    val state = projectFragment?.state()?.name?.lowercase()
    val stateChangedAt = projectFragment?.stateChangedAt()
    val url = projectFragment?.url()
    val urlsWeb = Web.builder()
        .project(url)
        .rewards("$url/rewards")
        .build()
    val urls = Urls.builder().web(urlsWeb).build()
    val displayPrelaunch = (projectFragment?.isLaunched ?: false).negate()

    return Project.builder()
        .backersCount(backersCount)
        .blurb(blurb)
        .category(category)
        .country(country)
        .createdAt(createdAt)
        .creator(creator)
        .currencySymbol(currencySymbol)
        .currencyTrailingCode(false) // - This field is available on V1 Configuration Object
        .displayPrelaunch(displayPrelaunch)
        .featuredAt(featuredAt)
        .friends(friends)
        .fxRate(fxRate)
        .deadline(deadline)
        .pledged(pledged)
        .goal(goal)
        .id(id)
        .isBacking(isBacking)
        .isStarred(isStarred)
        .launchedAt(launchedAt)
        .location(location)
        .name(name)
        .photo(photo) // - now we get the full size for field from GraphQL, but V1 provided several image sizes
        .prelaunchActivated(prelaunchActivated)
        .slug(slug)
        .staffPick(staffPicked)
        .state(state)
        .urls(urls)
        .stateChangedAt(stateChangedAt)
        .build()
}

private fun getPhoto(photoUrl: @Nullable String?): Photo? {
    val photo = photoUrl?.let {
        Photo.builder()
            .ed(photoUrl)
            .full(photoUrl)
            .little(photoUrl)
            .med(photoUrl)
            .small(photoUrl)
            .thumb(photoUrl)
            .build()
    }

    return photo
}

fun updateTransformer(post: fragment.Post?): Update {
    val id = decodeRelayId(post?.id()) ?: -1
    val author = User.builder()
        .id(decodeRelayId(post?.author()?.fragments()?.user()?.id()) ?: -1)
        .name(post?.author()?.fragments()?.user()?.name() ?: "")
        .avatar(
            Avatar.builder()
                .medium(post?.author()?.fragments()?.user()?.imageUrl())
                .build()
        )
        .build()

    val projectId = decodeRelayId(post?.project()?.id()) ?: -1

    val title = post?.title() ?: ""

    val publishedAt = post?.publishedAt()
    val updatedAt = post?.updatedAt()
    val sequence = post?.number() ?: 0

    val url = post?.project()?.url()
    val urlsWeb = Update.Urls.Web.builder()
        .update("$url/posts/$id")
        .build()
    val updateUrl = Update.Urls.builder().web(urlsWeb).build()

    val updateFreeformPost = post?.fragments()?.updateFreeformPost()
    val commentsCount = updateFreeformPost?.commentsCount()
    val body = updateFreeformPost?.body() as? String

    return Update.builder()
        .body(body)
        .commentsCount(commentsCount)
        .hasLiked(post?.isLiked)
        .id(id)
        .isPublic(post?.isPublic)
        .likesCount(post?.likesCount())
        .projectId(projectId)
        .publishedAt(publishedAt)
        .sequence(sequence)
        .title(title)
        .updatedAt(updatedAt)
        .urls(updateUrl)
        .user(author)
        .visible(post?.isVisible)
        .build()
}

fun commentTransformer(commentFr: fragment.Comment?): Comment {
    val badges: List<String> = commentFr?.authorBadges()?.map { badge ->
        badge?.rawValue() ?: ""
    } ?: emptyList()

    val author = User.builder()
        .id(decodeRelayId(commentFr?.author()?.fragments()?.user()?.id()) ?: -1)
        .name(commentFr?.author()?.fragments()?.user()?.name() ?: "")
        .avatar(
            Avatar.builder()
                .medium(commentFr?.author()?.fragments()?.user()?.imageUrl())
                .build()
        )
        .build()
    val id = decodeRelayId(commentFr?.id()) ?: -1
    val repliesCount = commentFr?.replies()?.totalCount() ?: 0
    val body = commentFr?.body() ?: ""
    val createdAt = commentFr?.createdAt()
    val deleted = commentFr?.deleted() ?: false
    val hasFlaggings = commentFr?.hasFlaggings() ?: false
    val sustained = commentFr?.sustained() ?: false
    val authorCanceled = commentFr?.authorCanceledPledge() ?: false
    val parentId = decodeRelayId(commentFr?.parentId())

    return Comment.builder()
        .id(id)
        .author(author)
        .repliesCount(repliesCount)
        .body(body)
        .authorBadges(badges)
        .cursor("")
        .createdAt(createdAt)
        .deleted(deleted)
        .hasFlaggings(hasFlaggings)
        .sustained(sustained)
        .authorCanceledPledge(authorCanceled)
        .parentId(parentId)
        .build()
}

/**
 * Transform the Backing GraphQL data structure into our own Backing data model
 * @param fragment.Baking projectFragment
 * @return Backing
 */
fun backingTransformer(backingGr: fragment.Backing?): Backing {
    val payment = backingGr?.paymentSource()?.fragments()?.payment()?.let { payment ->
        PaymentSource.builder()
            .state(payment.state().toString())
            .type(payment.type().rawValue())
            .paymentType(CreditCardPaymentType.CREDIT_CARD.rawValue())
            .id(payment.id())
            .expirationDate(payment.expirationDate())
            .lastFour(payment.lastFour())
            .build()
    }

    val addOns = backingGr?.addOns()?.let {
        return@let getAddOnsList(it)
    }

    val id = decodeRelayId(backingGr?.id())?.let { it } ?: 0

    val location = backingGr?.location()?.fragments()?.location()
    val locationId = decodeRelayId(location?.id())
    val projectId = decodeRelayId(backingGr?.project()?.fragments()?.project()?.id()) ?: -1
    val shippingAmount = backingGr?.shippingAmount()?.fragments()
    val items = backingGr?.reward()?.items()
    val reward = backingGr?.reward()?.fragments()?.reward()?.let { reward ->
        return@let rewardTransformer(
            reward,
            rewardItems = complexRewardItemsTransformer(items?.fragments()?.rewardItems())
        )
    }

    val backerData = backingGr?.backer()?.fragments()?.user()
    val nameBacker = backerData?.let { it.name() } ?: ""
    val backerId = decodeRelayId(backerData?.id()) ?: -1
    val avatar = Avatar.builder()
        .medium(backerData?.imageUrl())
        .build()
    val completedByBacker = backingGr?.backerCompleted() ?: false

    val backer = User.builder()
        .id(backerId)
        .name(nameBacker)
        .avatar(avatar)
        .build()
    val status = backingGr?.status()?.rawValue() ?: ""

    return Backing.builder()
        .amount(backingGr?.amount()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0)
        .bonusAmount(backingGr?.bonusAmount()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0)
        .paymentSource(payment)
        .backerId(backerId)
        .backerUrl(backerData?.imageUrl())
        .backerName(nameBacker)
        .backer(backer)
        .id(id)
        .reward(reward)
        .addOns(addOns)
        .rewardId(reward?.id())
        .locationId(locationId)
        .locationName(location?.displayableName())
        .pledgedAt(backingGr?.pledgedOn())
        .projectId(projectId)
        .sequence(backingGr?.sequence()?.toLong() ?: 0)
        .shippingAmount(shippingAmount?.amount()?.amount()?.toFloat() ?: 0f)
        .status(status)
        .cancelable(backingGr?.cancelable() ?: false)
        .completedByBacker(completedByBacker)
        .build()
}

/**
 * For addOns we receive this kind of data structure :[D, D, D, D, D, C, E, E]
 * and we need to transform it in : D(5),C(1),E(2)
 */
fun getAddOnsList(addOns: fragment.Backing.AddOns): List<Reward> {
    val rewardsList = addOns.nodes()?.map { node ->
        rewardTransformer(node.fragments().reward())
    }

    val mapHolder = mutableMapOf<Long, Reward>()

    rewardsList?.forEach {
        val q = mapHolder[it.id()]?.quantity() ?: 0
        mapHolder[it.id()] = it.toBuilder().quantity(q + 1).build()
    }

    return mapHolder.values.toList()
}

/**
 * Transform the Video GraphQL data structure into our own Video data model
 * @param fragment.Video video
 * @return Project
 */
fun videoTransformer(video: fragment.Video?): Video {
    val frame = video?.previewImageUrl()
    val base = video?.videoSources()?.base()?.src()
    val high = video?.videoSources()?.high()?.src()
    val hls = video?.videoSources()?.hls()?.src()

    return Video.builder()
        .base(base)
        .frame(frame)
        .high(high)
        .hls(hls)
        .build()
}

/**
 * Transform the fragment.ShippingRule GraphQL data structure into our own ShippingRules data model
 * @param fragment.ShippingRule
 * @return ShippingRule
 */
fun shippingRuleTransformer(rule: fragment.ShippingRule): ShippingRule {
    val cost = rule.cost()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0
    val location = rule.location()?.let {
        locationTransformer(it.fragments().location())
    }

    return ShippingRule.builder()
        .cost(cost)
        .location(location)
        .build()
}

/**
 * Transform the fragment.Location GraphQL data structure into our own Location data model
 * @param fragment.Location
 * @return Location
 */
fun locationTransformer(locationGR: fragment.Location?): Location {
    val id = decodeRelayId(locationGR?.id()) ?: -1
    val country = locationGR?.country() ?: ""
    val displayName = locationGR?.displayableName()
    val name = locationGR?.name()

    return Location.builder()
        .id(id)
        .country(country)
        .displayableName(displayName)
        .name(name)
        .build()
}

fun shippingRulesListTransformer(shippingRulesExpanded: List<fragment.ShippingRule>): ShippingRulesEnvelope {
    val shippingRulesList: List<ShippingRule> = shippingRulesExpanded?.mapNotNull { shippingRule ->
        shippingRuleTransformer(shippingRule)
    } ?: emptyList<ShippingRule>()

    return ShippingRulesEnvelope
        .builder()
        .shippingRules(shippingRulesList)
        .build()
}
