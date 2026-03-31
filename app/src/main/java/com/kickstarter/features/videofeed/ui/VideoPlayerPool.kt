package com.kickstarter.features.videofeed.ui

import android.content.Context
import androidx.annotation.MainThread
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.kickstarter.libs.utils.extensions.initializeExoplayer
import kotlin.math.abs

/**
 * A fixed-size pool of [ExoPlayer] instances implementing a sliding window strategy for a
 * vertical video feed.
 *
 * The pool keeps a buffer window larger than the visible window: with the default [poolSize]
 * of 5 and [beyondViewportPageCount] of 1, pages [currentPage-2 … currentPage+2] are buffered
 * while only [currentPage-1, currentPage, currentPage+1] are composed. Scrolling back up to
 * two pages in either direction plays instantly from the existing buffer.
 *
 * Pages inside the composition window acquire their player via [getPlayer] (called from
 * `remember` inside the pager). Pages outside the window — current±2 — are pre-buffered
 * proactively via [preload], called from a `LaunchedEffect` that observes `currentPage`.
 * When the user scrolls and a new page needs a slot, the pool recycles the slot farthest
 * from the new current page.
 *
 * **Pool size:** must be >= `beyondViewportPageCount * 2 + 1`. The extra slots beyond that
 * minimum (default 5 vs minimum 3) are the proactive buffer — increase them to extend how
 * far back/forward instant playback is preserved.
 *
 * All public methods are [@MainThread] — the pool is only ever accessed from Compose
 * composition, so no additional synchronisation is required.
 *
 * Usage:
 * ```
 * val pool = remember { VideoPlayerPool(context) }
 * DisposableEffect(pool) { onDispose { pool.releaseAll() } }
 *
 * LaunchedEffect(pagerState.currentPage) {
 *     pool.preload(pagerState.currentPage + 2, urls[currentPage + 2], pagerState.currentPage)
 *     pool.preload(pagerState.currentPage - 2, urls[currentPage - 2], pagerState.currentPage)
 * }
 *
 * VerticalPager(beyondViewportPageCount = 1) { page ->
 *     val player = remember(page, url) { pool.getPlayer(page, url, pagerState.currentPage) }
 *     KSVideoPlayer(player = player, isActive = page == pagerState.currentPage)
 * }
 * ```
 */
class VideoPlayerPool(
    context: Context,
    poolSize: Int = 5
) {
    init {
        require(poolSize >= 1) { "poolSize must be >= 1, was $poolSize" }
    }

    private val players: List<ExoPlayer> = List(poolSize) {
        context.initializeExoplayer().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    private val assignedPage = arrayOfNulls<Int>(poolSize)
    private val assignedUrl = arrayOfNulls<String>(poolSize)

    /**
     * Returns the [ExoPlayer] for [pageIndex], loading [videoUrl] if the slot is not yet
     * assigned or the URL has changed.
     *
     * When all slots are occupied, the slot whose page is farthest from [currentPage] is evicted
     * and reused — ensuring adjacent pages are always pre-buffered.
     *
     * This method is idempotent: repeated calls for the same [pageIndex] + [videoUrl] return
     * the same player without modifying state.
     */
    @MainThread
    fun getPlayer(pageIndex: Int, videoUrl: String, currentPage: Int): ExoPlayer {
        // Fast path: slot already assigned for this page.
        val existingSlot = assignedPage.indexOfFirst { it == pageIndex }
        if (existingSlot != -1) {
            if (assignedUrl[existingSlot] != videoUrl) {
                // URL changed (e.g. live data refresh) — reload in-place without eviction.
                players[existingSlot].loadVideo(videoUrl)
                assignedUrl[existingSlot] = videoUrl
            }
            return players[existingSlot]
        }

        // Slow path: find a free slot, or evict the slot whose page is farthest from currentPage.
        val targetSlot = assignedPage.indexOfFirst { it == null }
            .takeIf { it != -1 }
            ?: assignedPage.indices.maxBy { slot ->
                assignedPage[slot]?.let { abs(it - currentPage) } ?: 0
            }

        assignedPage[targetSlot] = pageIndex
        assignedUrl[targetSlot] = videoUrl
        players[targetSlot].loadVideo(videoUrl)

        return players[targetSlot]
    }

    /**
     * Pre-buffers the video at [pageIndex] without returning the player. Call this from a
     * `LaunchedEffect` for pages that are outside the composition window (current±2) so their
     * buffer is ready before the user scrolls to them.
     *
     * Delegates to [getPlayer] — idempotent, safe to call repeatedly.
     */
    @MainThread
    fun preload(pageIndex: Int, videoUrl: String, currentPage: Int) {
        getPlayer(pageIndex, videoUrl, currentPage)
    }

    /**
     * Releases all [ExoPlayer] instances. Must be called when the owning component is destroyed
     * (e.g. inside a `DisposableEffect` `onDispose` block in the Activity's `setContent`).
     */
    @MainThread
    fun releaseAll() {
        players.forEach { it.release() }
        assignedPage.fill(null)
        assignedUrl.fill(null)
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Replaces the current media item with [url] and starts buffering.
     * [playWhenReady] is kept false — the composable drives playback via [isActive].
     */
    private fun ExoPlayer.loadVideo(url: String) {
        setMediaItem(MediaItem.fromUri(url))
        playWhenReady = false
        prepare()
    }
}
