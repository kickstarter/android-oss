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
 * stops whatever video is currently playing. [park] stops a player whose page has scrolled off —
 * freeing its decoder while keeping the instance for reuse — so the number of LIVE decoders tracks
 * only the pages on screen (~3), the same footprint as building one player per visible page.
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
 * Sizing: [maxPlayers] should be >= the number of pages composed at once (the pager uses
 * beyondViewportPageCount = 1, i.e. current ± 1 = 3 pages). Recycling only ever reuses a [park]ed
 * (off-screen) player, never one bound to an on-screen page: the deque is ordered by acquire-recency,
 * not visibility, so without that rule the least-recently-used entry could be the visible page and
 * recycling it would freeze the playing video. If every pooled player is on screen, [acquire] builds
 * a fresh instance rather than steal a visible one, so [maxPlayers] is a soft floor.
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
     * changed); otherwise, when the pool is full, the least-recently-used *parked* player is recycled,
     * and only if none is parked is a new player created.
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

        // When full, reuse the least-recently-used PARKED player. Never recycle a non-parked player:
        // it is bound to an on-screen page, so stopping and rebinding it would freeze the visible
        // video. If every pooled player is on screen, build a fresh one rather than steal a visible one.
        val pooled = (if (players.size >= maxPlayers) recycleParkedLeastRecentlyUsed(key, videoUrl) else null)
            ?: PooledPlayer(playerFactory(), key, videoUrl).also { bindMedia(it.player, videoUrl) }

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

    /**
     * Recycles the least-recently-used *parked* player, or returns null if none is parked (every
     * pooled player is currently on screen). The deque is ordered LRU→MRU, so the first parked entry
     * is the least-recently-used off-screen player.
     */
    private fun recycleParkedLeastRecentlyUsed(key: Any, videoUrl: String): PooledPlayer? {
        val lru = players.firstOrNull { it.parked } ?: return null
        players.remove(lru)
        byKey.remove(lru.key)
        // Reuse the instance: stop playback and rebind. Deliberately NO release() — releasing a
        // MediaCodec on the main thread mid-scroll is the stall this pool exists to prevent.
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
        // — composed and disposed within the debounce — never grab a decoder.
        player.setMediaItem(MediaItem.fromUri(videoUrl))
        player.repeatMode = Player.REPEAT_MODE_ONE
    }

    companion object {
        const val DEFAULT_MAX_PLAYERS = 5
    }
}
