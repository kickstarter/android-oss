package com.kickstarter.features.videofeed.player

import androidx.media3.exoplayer.ExoPlayer
import com.kickstarter.KSRobolectricTestCase
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class VideoPlayerPoolTest : KSRobolectricTestCase() {

    private val created = mutableListOf<ExoPlayer>()

    private fun pool(maxPlayers: Int = 3) = VideoPlayerPool(maxPlayers = maxPlayers) {
        mock(ExoPlayer::class.java).also { created.add(it) }
    }

    @Test
    fun `acquire reuses the same player for the same key`() {
        val pool = pool()

        val first = pool.acquire(key = 0, videoUrl = URL_A)
        val again = pool.acquire(key = 0, videoUrl = URL_A)

        assertSame(first, again)
        assertEquals(1, created.size)
    }

    @Test
    fun `acquire creates a distinct player per key up to capacity`() {
        val pool = pool(maxPlayers = 3)

        val p0 = pool.acquire(0, URL_A)
        val p1 = pool.acquire(1, URL_B)
        val p2 = pool.acquire(2, URL_C)

        assertEquals(3, created.size)
        assertNotSame(p0, p1)
        assertNotSame(p1, p2)
        assertNotSame(p0, p2)
    }

    @Test
    fun `acquiring beyond capacity recycles the least-recently-used player without releasing it`() {
        val pool = pool(maxPlayers = 3)

        val lru = pool.acquire(0, URL_A) // becomes least-recently-used after the next two
        pool.acquire(1, URL_B)
        pool.acquire(2, URL_C)

        val recycled = pool.acquire(3, URL_D) // pool is full → must evict key 0

        assertEquals(3, created.size) // no new player created — the LRU was reused
        assertSame(lru, recycled) // ...and it is the same instance
        verify(lru).stop() // stopped before being rebound to the new media
        // The whole point of the pool: release() must NEVER run on the scroll path.
        created.forEach { verify(it, never()).release() }
    }

    @Test
    fun `re-acquiring a key after it was evicted does not resurrect the old binding`() {
        val pool = pool(maxPlayers = 3)

        pool.acquire(0, URL_A)
        pool.acquire(1, URL_B)
        pool.acquire(2, URL_C)
        pool.acquire(3, URL_D) // evicts key 0
        pool.acquire(0, URL_A) // key 0 is gone → evicts the new LRU, still no new instance

        assertEquals(3, created.size)
        created.forEach { verify(it, never()).release() }
    }

    @Test
    fun `release releases every pooled player and empties the pool`() {
        val pool = pool(maxPlayers = 3)
        pool.acquire(0, URL_A)
        pool.acquire(1, URL_B)

        pool.release()

        created.forEach { verify(it).release() }

        // After release the pool is empty, so the next acquire builds a fresh player.
        pool.acquire(0, URL_A)
        assertEquals(3, created.size)
    }

    @Test
    fun `a long fast scroll never grows past capacity nor releases a player mid-scroll`() {
        val pool = pool(maxPlayers = 3)

        for (page in 0 until 30) {
            pool.acquire(page, "https://videos.test/$page.m3u8")
        }

        assertEquals(3, created.size)
        created.forEach { verify(it, never()).release() }
    }

    @Test
    fun `park stops the player to free its decoder but keeps it pooled for reuse`() {
        val pool = pool()
        val player = pool.acquire(0, URL_A)

        pool.park(0)

        verify(player).stop() // decoder freed while off-screen...
        verify(player, never()).release() // ...but never released on the scroll path

        val again = pool.acquire(0, URL_A)
        assertSame(player, again) // same instance reused, never re-created
        assertEquals(1, created.size)
    }

    @Test
    fun `re-acquiring a parked player reuses it without re-stopping`() {
        val pool = pool()
        val player = pool.acquire(0, URL_A)
        pool.park(0) // stop() once

        val again = pool.acquire(0, URL_A) // clears the parked flag, returns the same instance

        assertSame(player, again)
        verify(player, times(1)).stop() // only the park's stop, none added by re-acquire
    }

    @Test
    fun `the pool never prepares - the consumer owns prepare`() {
        // prepare() allocates the hardware decoder; it is deliberately deferred to KSVideoPlayer's
        // debounce so flung-past pages never allocate one. The pool must never call it.
        val pool = pool(maxPlayers = 3)
        val p0 = pool.acquire(0, URL_A) // new player
        pool.acquire(1, URL_B)
        pool.acquire(2, URL_C)
        pool.acquire(3, URL_D) // recycles p0
        pool.park(1)
        pool.acquire(1, URL_B) // re-acquire parked

        created.forEach { verify(it, never()).prepare() }
        verify(p0).stop() // recycle still stops before rebinding (frees the old decoder)
    }

    @Test
    fun `park is a no-op for an unknown key`() {
        val pool = pool()
        val player = pool.acquire(0, URL_A)

        pool.park(99) // never pooled

        verify(player, never()).stop()
    }

    @Test
    fun `park does not stop a player whose key was already recycled to another page`() {
        val pool = pool(maxPlayers = 3)
        val evicted = pool.acquire(0, URL_A)
        pool.acquire(1, URL_B)
        pool.acquire(2, URL_C)
        pool.acquire(3, URL_D) // recycles key 0's player (one stop) and rebinds it to key 3

        pool.park(0) // key 0 no longer maps to that player → must not stop the now-key-3 player

        verify(evicted, times(1)).stop() // only the recycle's stop, none added by park
    }

    companion object {
        private const val URL_A = "https://videos.test/a.m3u8"
        private const val URL_B = "https://videos.test/b.m3u8"
        private const val URL_C = "https://videos.test/c.m3u8"
        private const val URL_D = "https://videos.test/d.m3u8"
    }
}
