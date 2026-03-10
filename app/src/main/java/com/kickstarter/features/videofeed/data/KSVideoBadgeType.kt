package com.kickstarter.features.videofeed.data

sealed class KSVideoBadgeType {
    object ProjectWeLove : KSVideoBadgeType()
    data class DaysLeft(val text: String) : KSVideoBadgeType()
    object JustLaunched : KSVideoBadgeType()
    object FirstTimeCreator : KSVideoBadgeType()
    object ByASuperbacker : KSVideoBadgeType()
    object NearYou : KSVideoBadgeType()
    object NSFW : KSVideoBadgeType()
    object AlmostThere : KSVideoBadgeType()
    object Trending : KSVideoBadgeType()
    object Popular : KSVideoBadgeType()
    object HotRightNow : KSVideoBadgeType()
}
