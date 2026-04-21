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
        val creator = User.builder()
            .name(node.project.creator?.name)
            .avatar(
                Avatar.builder()
                    .medium(node.project.creator?.imageUrl)
                    .build()
            )
            .build()
        val category = Category.builder()
            .name(node.project.category?.name)
            .build()
        val project = Project.builder()
            .id(decodeRelayId(node.project.id) ?: -1)
            .name(node.project.name)
            .slug(node.project.slug)
            .percentFunded(node.project.percentFunded)
            .deadline(node.project.deadlineAt)
            .launchedAt(node.project.launchedAt)
            .creator(creator)
            .category(category)
            .build()
        VideoFeedItem(badges = badges, project = project)
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
