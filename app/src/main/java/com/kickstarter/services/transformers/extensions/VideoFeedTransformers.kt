package com.kickstarter.services.transformers.extensions

import com.kickstarter.VideoFeedQuery
import com.kickstarter.features.videofeed.data.KSVideoBadgeType
import com.kickstarter.features.videofeed.data.VideoFeedEnvelope
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.models.Avatar
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.type.BadgeTypeEnum

fun VideoFeedQuery.VideoFeed?.toVideoFeedEnvelope(): VideoFeedEnvelope {
    val items = this?.nodes?.mapNotNull { node ->
        node ?: return@mapNotNull null
        val badges = node.badges.mapNotNull { badge ->
            when (badge.type) {
                BadgeTypeEnum.PROJECT_WE_LOVE -> KSVideoBadgeType.ProjectWeLove
                BadgeTypeEnum.DAYS_LEFT -> KSVideoBadgeType.DaysLeft(badge.text)
                BadgeTypeEnum.JUST_LAUNCHED -> KSVideoBadgeType.JustLaunched
                BadgeTypeEnum.TRENDING -> KSVideoBadgeType.Trending
                else -> null
            }
        }
        val frag = node.project.videoFeedProject
        val creator = User.builder()
            .name(frag.creator?.name)
            .avatar(
                Avatar.builder()
                    .medium(frag.creator?.imageUrl)
                    .build()
            )
            .build()
        val category = Category.builder()
            .name(frag.category?.name)
            .build()
        val project = Project.builder()
            .id(decodeRelayId(frag.id) ?: -1)
            .name(frag.name)
            .slug(frag.slug)
            .percentFunded(frag.percentFunded)
            .deadline(frag.deadlineAt)
            .launchedAt(frag.launchedAt)
            .backersCount(frag.backersCount)
            .watchesCount(frag.watchesCount)
            .sharesCount(frag.sharesCount)
            .isStarred(frag.isWatched)
            .pledged(frag.pledged?.amount?.amount?.toDouble() ?: 0.0)
            .currencySymbol(frag.pledged?.amount?.symbol ?: "")
            .creator(creator)
            .category(category)
            .build()
        VideoFeedItem(
            badges = badges,
            project = project,
            hlsUrl = frag.verticalVideo?.videoSources?.hls?.src
        )
    } ?: emptyList()

    val pageInfo = this?.pageInfo?.pageInfo?.let {
        PageInfoEnvelope.builder()
            .hasNextPage(it.hasNextPage)
            .endCursor(it.endCursor ?: "")
            .hasPreviousPage(it.hasPreviousPage)
            .startCursor(it.startCursor ?: "")
            .build()
    }

    return VideoFeedEnvelope(items = items, pageInfo = pageInfo)
}
