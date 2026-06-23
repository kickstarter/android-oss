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
 * Capping LIVE decoders (the reason [park] exists): a *prepared* [ExoPlayer] holds a hardware
 * [android.media.MediaCodec] video decoder. Devices have a small budget of concurrent decoders, and
 * when it is exceeded Android's media resource manager starts RECLAIMING the app's decoders — which
 * stops whatever video is currently playing (observed on a Pixel 6a as repeated
 * "MediaCodec: Released by resource manager" while fast-scrolling, with the visible video freezing).
 * So keeping a player merely *retained* is fine, but keeping it *prepared* is not. [park] stops a
 * player whose page has scrolled off — freeing its decoder while keeping the instance for reuse — so
 * the number of LIVE decoders tracks only the pages on screen (~3), the same footprint as building
 * one player per visible page.
 *
 * Preparation is the consumer's job: [acquire] binds the media but does NOT [ExoPlayer.prepare] (the
 * step that allocates the decoder). KSVideoPlayer prepares behind a short debounce, so pages flung
 * past — composed and disposed within the debounce — never allocate a decoder, capping the
 * allocation rate. So the pool here only manages instance reuse and decoder *freeing* ([park]).
 *
 * Contract:
 *  - [acquire] and [park] are the only calls on the scroll path and NEVER call [ExoPlayer.release].
 *  - [release] is called once, when the feed leaves the screen, and releases every pooled player.
 *  - Not thread-safe: all calls must come from the main thread (the players' application thread),
 *    which is where Compose composition and the pager already run.
 *
 * Sizing: [maxPlayers] must be >= the number of pages composed at once. The pager uses
 * beyondViewportPageCount = 1 (current ± 1 = 3 pages); the default leaves headroom so a player
 * bound to a still-composed page is never recycled out from under it during a fast fling. Because
 * off-screen players are [park]ed, the extra retained instances are stopped (no decoder, no buffer),
 * so the headroom costs idle instances rather than live decoders.
 */
class VideoPlayerPool(
    private val maxPlayers: Int = DEFAULT_MAX_PLAYERS,
    private val playerFactory: () -> ExoPlayer,
) {
    init {
        require(maxPlayers > 0) { "maxPlayers must be > 0, was $maxPlayers" }
    }

    private class PooledPlayer(
        val player: ExoPlayer,
        var key: Any,
        var videoUrl: String,
        // True once [park] has stopped this player: its decoder/buffer are released and it must be
        // re-prepared before it can play again.
        var parked: Boolean = false,
    )

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
            } else if (existing.parked) {
                // Scrolled back to a page we parked: clear the flag. The consumer re-prepares (it has
                // to anyway, since the decoder was freed) on its debounce, re-acquiring a decoder.
                existing.parked = false
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

    /**
     * Stops the player bound to [key], freeing its hardware [android.media.MediaCodec] decoder and
     * buffer while keeping the instance pooled for reuse. Call when a page scrolls out of the
     * composed window so only the visible pages hold a live decoder; the consumer re-prepares it after
     * its next [acquire]. Deliberately NO release() — that whole-player teardown on the main thread is
     * the stall this pool exists to prevent.
     *
     * No-op if [key] is no longer pooled (already recycled to another page) or is already parked, so
     * it is safe to call from a page's disposal effect.
     */
    fun park(key: Any) {
        val pooled = byKey[key] ?: return
        if (pooled.parked) return
        pooled.player.stop()
        pooled.parked = true
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
        lru.parked = false
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
        // Set the media but do NOT prepare(). prepare() is what allocates the hardware decoder, and
        // the consumer (KSVideoPlayer) defers it behind a short debounce so pages the user FLINGS past
        // — composed and disposed within the debounce — never grab a decoder. That caps the decoder-
        // allocation rate that makes the OS reclaim the playing video's decoder during fast scroll.
        player.setMediaItem(MediaItem.fromUri(videoUrl))
        player.repeatMode = Player.REPEAT_MODE_ONE
    }

    companion object {
        const val DEFAULT_MAX_PLAYERS = 5
    }
}
