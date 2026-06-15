package com.kickstarter.features.videofeed.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * A small, bounded pool of reusable [ExoPlayer] instances shared across the pages of the video feed
 * pager.
 *
 * Why this exists: creating and — crucially — *releasing* an [ExoPlayer] per page on every swipe
 * tears down a hardware [android.media.MediaCodec] on the main thread. During a fast scroll those
 * synchronous teardowns stack up and freeze the UI, while audio (decoded on ExoPlayer's own thread)
 * keeps playing through the freeze. Reusing a fixed set of players removes that churn: a player is
 * built once and, when the pool is full, the least-recently-used one is *rebound* to the new page's
 * media rather than released.
 *
 * Contract:
 *  - [acquire] is the only call on the scroll path and NEVER calls [ExoPlayer.release].
 *  - [release] is called once, when the feed leaves the screen, and releases every pooled player.
 *  - Not thread-safe: all calls must come from the main thread (the players' application thread),
 *    which is where Compose composition and the pager already run.
 *
 * Sizing: [maxPlayers] must be >= the number of pages composed at once. The pager uses
 * beyondViewportPageCount = 1 (current ± 1 = 3 pages); the default leaves headroom so a player
 * bound to a still-composed page is never recycled out from under it during a fast fling. Each
 * retained player keeps a prepared decoder + buffer alive, so this also bounds the feed's video
 * memory; raising it trades memory for headroom.
 */
class VideoPlayerPool(
    private val maxPlayers: Int = DEFAULT_MAX_PLAYERS,
    private val playerFactory: () -> ExoPlayer,
) {
    private class PooledPlayer(val player: ExoPlayer, var key: Any, var videoUrl: String)

    // Ordered least-recently-used (front) → most-recently-used (back).
    private val players = ArrayDeque<PooledPlayer>()
    private val byKey = HashMap<Any, PooledPlayer>()

    /**
     * Returns a player bound to [key] playing [videoUrl], reusing an existing instance whenever
     * possible. If [key] is already pooled it is returned as-is (rebinding only if [videoUrl]
     * changed); otherwise a new player is created until [maxPlayers] is reached, after which the
     * least-recently-used player is recycled — stopped and rebound, but never released.
     */
    fun acquire(key: Any, videoUrl: String): ExoPlayer {
        byKey[key]?.let { existing ->
            markMostRecentlyUsed(existing)
            if (existing.videoUrl != videoUrl) {
                existing.videoUrl = videoUrl
                bindMedia(existing.player, videoUrl)
            }
            return existing.player
        }

        val pooled = if (players.size >= maxPlayers) {
            recycleLeastRecentlyUsed(key, videoUrl)
        } else {
            PooledPlayer(playerFactory(), key, videoUrl).also { bindMedia(it.player, videoUrl) }
        }

        players.addLast(pooled)
        byKey[pooled.key] = pooled
        return pooled.player
    }

    /** Releases every pooled player and empties the pool. Call once when the feed is disposed. */
    fun release() {
        players.forEach { it.player.release() }
        players.clear()
        byKey.clear()
    }

    private fun recycleLeastRecentlyUsed(key: Any, videoUrl: String): PooledPlayer {
        val lru = players.removeFirst()
        byKey.remove(lru.key)
        // Reuse the instance: stop playback and rebind. Deliberately NO release() here — releasing a
        // MediaCodec on the main thread mid-scroll is exactly the stall this pool exists to prevent.
        lru.player.stop()
        lru.key = key
        lru.videoUrl = videoUrl
        bindMedia(lru.player, videoUrl)
        return lru
    }

    private fun markMostRecentlyUsed(pooled: PooledPlayer) {
        if (players.lastOrNull() !== pooled) {
            players.remove(pooled)
            players.addLast(pooled)
        }
    }

    private fun bindMedia(player: ExoPlayer, videoUrl: String) {
        player.setMediaItem(MediaItem.fromUri(videoUrl))
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.prepare()
    }

    companion object {
        const val DEFAULT_MAX_PLAYERS = 5
    }
}
