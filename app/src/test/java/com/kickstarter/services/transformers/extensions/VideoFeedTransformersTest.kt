package com.kickstarter.services.transformers.extensions

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.VideoFeedQuery
import com.kickstarter.features.videofeed.data.KSVideoBadgeType
import com.kickstarter.features.videofeed.data.VideoFeedEnvelope
import com.kickstarter.fragment.Amount
import com.kickstarter.fragment.PageInfo
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.type.BadgeTypeEnum
import com.kickstarter.type.CurrencyCode
import org.joda.time.DateTime
import org.junit.Test

class VideoFeedTransformersTest : KSRobolectricTestCase() {

    @Test
    fun `toVideoFeedEnvelope with null receiver returns empty envelope`() {
        val envelope = (null as VideoFeedQuery.VideoFeed?).toVideoFeedEnvelope()
        assertEquals(VideoFeedEnvelope(), envelope)
    }

    @Test
    fun `toVideoFeedEnvelope with empty nodes returns empty items`() {
        val feed = fixtureVideoFeed(nodes = emptyList())
        val envelope = feed.toVideoFeedEnvelope()

        assertTrue(envelope.items.isEmpty())
        assertNotNull(envelope.pageInfo)
    }

    @Test
    fun `toVideoFeedEnvelope with null node in list filters it out`() {
        val feed = fixtureVideoFeed(nodes = listOf(null, fixtureNode()))
        val envelope = feed.toVideoFeedEnvelope()

        assertEquals(1, envelope.items.size)
    }

    @Test
    fun `toVideoFeedEnvelope maps pageInfo correctly`() {
        val feed = fixtureVideoFeed()
        val envelope = feed.toVideoFeedEnvelope()

        assertEquals(true, envelope.pageInfo?.hasNextPage)
        assertEquals(false, envelope.pageInfo?.hasPreviousPage)
        assertEquals("cursor_end", envelope.pageInfo?.endCursor)
        assertEquals("cursor_start", envelope.pageInfo?.startCursor)
    }

    @Test
    fun `toVideoFeedEnvelope maps project fields correctly`() {
        val deadline = DateTime.now().plusDays(7)
        val launched = DateTime.now().minusDays(10)
        val node = fixtureNode(
            project = fixtureProject(
                id = "UHJvamVjdC0xMjM=",
                name = "Space Explorer",
                slug = "space-explorer",
                percentFunded = 82,
                deadlineAt = deadline,
                launchedAt = launched,
                creator = VideoFeedQuery.Creator(name = "Jane Doe", imageUrl = "https://example.com/jane.jpg"),
                category = VideoFeedQuery.Category(name = "Technology")
            )
        )
        val envelope = fixtureVideoFeed(nodes = listOf(node)).toVideoFeedEnvelope()
        val project = envelope.items.first().project

        assertEquals(decodeRelayId("UHJvamVjdC0xMjM="), project.id())
        assertEquals("Space Explorer", project.name())
        assertEquals("space-explorer", project.slug())
        assertEquals(82, project.percentFunded())
        assertEquals(deadline, project.deadline())
        assertEquals(launched, project.launchedAt())
        assertEquals("Jane Doe", project.creator()?.name())
        assertEquals("https://example.com/jane.jpg", project.creator()?.avatar()?.medium())
        assertEquals("Technology", project.category()?.name())
    }

    @Test
    fun `toVideoFeedEnvelope maps all known badge types`() {
        val node = fixtureNode(
            badges = listOf(
                fixtureBadge(BadgeTypeEnum.PROJECT_WE_LOVE, "Project We Love"),
                fixtureBadge(BadgeTypeEnum.DAYS_LEFT, "3 days left"),
                fixtureBadge(BadgeTypeEnum.JUST_LAUNCHED, "Just Launched"),
                fixtureBadge(BadgeTypeEnum.TRENDING, "Trending"),
            )
        )
        val badges = fixtureVideoFeed(nodes = listOf(node)).toVideoFeedEnvelope().items.first().badges

        assertEquals(4, badges.size)
        assertTrue(badges[0] is KSVideoBadgeType.ProjectWeLove)
        assertTrue(badges[1] is KSVideoBadgeType.DaysLeft)
        assertEquals("3 days left", (badges[1] as KSVideoBadgeType.DaysLeft).text)
        assertTrue(badges[2] is KSVideoBadgeType.JustLaunched)
        assertTrue(badges[3] is KSVideoBadgeType.Trending)
    }

    @Test
    fun `toVideoFeedEnvelope filters out unknown badge types`() {
        val node = fixtureNode(
            badges = listOf(
                fixtureBadge(BadgeTypeEnum.PROJECT_WE_LOVE, "Project We Love"),
                fixtureBadge(BadgeTypeEnum.UNKNOWN__, "Some future badge"),
            )
        )
        val badges = fixtureVideoFeed(nodes = listOf(node)).toVideoFeedEnvelope().items.first().badges

        assertEquals(1, badges.size)
        assertTrue(badges[0] is KSVideoBadgeType.ProjectWeLove)
    }

    @Test
    fun `toVideoFeedEnvelope maps hlsUrl from lastUploadedVerticalVideo`() {
        val expectedUrl = "https://example.com/video.m3u8"
        val node = fixtureNode(
            project = fixtureProject(
                lastUploadedVerticalVideo = VideoFeedQuery.LastUploadedVerticalVideo(
                    id = "VmlkZW8t",
                    previewImageUrl = "https://example.com/thumb.jpg",
                    videoSources = VideoFeedQuery.VideoSources(
                        hls = VideoFeedQuery.Hls(src = expectedUrl)
                    )
                )
            )
        )
        val item = fixtureVideoFeed(nodes = listOf(node)).toVideoFeedEnvelope().items.first()

        assertEquals(expectedUrl, item.hlsUrl)
    }

    @Test
    fun `toVideoFeedEnvelope maps null lastUploadedVerticalVideo to null hlsUrl`() {
        val node = fixtureNode(project = fixtureProject(lastUploadedVerticalVideo = null))
        val item = fixtureVideoFeed(nodes = listOf(node)).toVideoFeedEnvelope().items.first()

        assertNull(item.hlsUrl)
    }

    @Test
    fun `toVideoFeedEnvelope maps backersCount correctly`() {
        val node = fixtureNode(project = fixtureProject(backersCount = 431))
        val project = fixtureVideoFeed(nodes = listOf(node)).toVideoFeedEnvelope().items.first().project

        assertEquals(431, project.backersCount())
    }

    @Test
    fun `toVideoFeedEnvelope maps pledged amount and currency symbol correctly`() {
        val node = fixtureNode(
            project = fixtureProject(
                pledged = VideoFeedQuery.Pledged(
                    __typename = "Money",
                    amount = Amount(amount = "50134.00", currency = CurrencyCode.USD, symbol = "$")
                )
            )
        )
        val project = fixtureVideoFeed(nodes = listOf(node)).toVideoFeedEnvelope().items.first().project

        assertEquals(50134.0, project.pledged(), 0.01)
        assertEquals("$", project.currencySymbol())
    }

    @Test
    fun `toVideoFeedEnvelope falls back to 0 and empty string when pledged amount is null`() {
        val node = fixtureNode(
            project = fixtureProject(
                pledged = VideoFeedQuery.Pledged(
                    __typename = "Money",
                    amount = Amount(amount = null, currency = null, symbol = null)
                )
            )
        )
        val project = fixtureVideoFeed(nodes = listOf(node)).toVideoFeedEnvelope().items.first().project

        assertEquals(0.0, project.pledged(), 0.0)
        assertEquals("", project.currencySymbol())
    }
}

// - Fixtures

private fun fixtureVideoFeed(
    nodes: List<VideoFeedQuery.Node?>? = listOf(fixtureNode()),
) = VideoFeedQuery.VideoFeed(
    pageInfo = VideoFeedQuery.PageInfo(
        __typename = "PageInfo",
        pageInfo = PageInfo(
            hasPreviousPage = false,
            hasNextPage = true,
            startCursor = "cursor_start",
            endCursor = "cursor_end"
        )
    ),
    nodes = nodes
)

private fun fixtureNode(
    badges: List<VideoFeedQuery.Badge> = listOf(fixtureBadge(BadgeTypeEnum.PROJECT_WE_LOVE, "Project We Love")),
    project: VideoFeedQuery.Project = fixtureProject()
) = VideoFeedQuery.Node(badges = badges, project = project)

private fun fixtureProject(
    id: String = "UHJvamVjdC0x",
    name: String = "Test Project",
    slug: String = "test-project",
    percentFunded: Int = 50,
    deadlineAt: DateTime? = null,
    launchedAt: DateTime? = null,
    backersCount: Int = 150,
    pledged: VideoFeedQuery.Pledged = VideoFeedQuery.Pledged(
        __typename = "Money",
        amount = Amount(amount = "5000.00", currency = CurrencyCode.USD, symbol = "$")
    ),
    creator: VideoFeedQuery.Creator? = VideoFeedQuery.Creator(name = "Creator", imageUrl = "https://example.com/avatar.jpg"),
    category: VideoFeedQuery.Category? = VideoFeedQuery.Category(name = "Art"),
    lastUploadedVerticalVideo: VideoFeedQuery.LastUploadedVerticalVideo? = VideoFeedQuery.LastUploadedVerticalVideo(
        id = "VmlkZW8t",
        previewImageUrl = "https://example.com/thumb.jpg",
        videoSources = VideoFeedQuery.VideoSources(
            hls = VideoFeedQuery.Hls(src = "https://example.com/video.m3u8")
        )
    )
) = VideoFeedQuery.Project(
    id = id,
    pid = 1,
    name = name,
    slug = slug,
    percentFunded = percentFunded,
    deadlineAt = deadlineAt,
    launchedAt = launchedAt,
    backersCount = backersCount,
    pledged = pledged,
    creator = creator,
    category = category,
    lastUploadedVerticalVideo = lastUploadedVerticalVideo
)

private fun fixtureBadge(type: BadgeTypeEnum, text: String, icon: String? = null) =
    VideoFeedQuery.Badge(type = type, text = text, icon = icon)
