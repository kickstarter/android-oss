package com.kickstarter.features.videofeed.data

import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope

/**
 * Sealed class representing the different types of badges that can be displayed on a video feed item.
 */
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

/**
 * Data class representing a single item in the video feed.
 *
 * @param badges List of [KSVideoBadgeType] to display for this item.
 * @param project The [Project] associated with this video.
 * @param hlsUrl The URL of the HLS video stream.
 * @param previewImageUrl Optional URL for the video's preview/poster image.
 * @param videoId Unique identifier for the video.
 */
data class VideoFeedItem(
    val badges: List<KSVideoBadgeType>,
    val project: Project,
    val hlsUrl: String?,
    val previewImageUrl: String? = null,
    val videoId: Long = 0
)

/**
 * Envelope for the video feed response, containing items and pagination info.
 *
 * @param items The list of [VideoFeedItem] returned.
 * @param pageInfo Pagination information for the next request.
 */
data class VideoFeedEnvelope(
    val items: List<VideoFeedItem> = emptyList(),
    val pageInfo: PageInfoEnvelope? = null
)
