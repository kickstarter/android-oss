package com.kickstarter.features.videofeed.data

import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

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

data class VideoFeedItem(
    val badges: List<KSVideoBadgeType>,
    val project: Project,
    val hlsUrl: String?
)

data class VideoFeedEnvelope(
    val items: List<VideoFeedItem> = emptyList(),
    val pageInfo: PageInfoEnvelope? = null
)
