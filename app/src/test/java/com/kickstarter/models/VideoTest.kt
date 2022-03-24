package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.VideoFactory
import org.junit.Test

class VideoTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val video = Video.builder()
            .base("https://www.kickstarter.com/project/base.mp4")
            .frame("https://www.kickstarter.com/project/frame.mp4")
            .high("https://www.kickstarter.com/project/high.mp4")
            .hls("https://ksr-video.imgix.net/projects/3275127/video-865539-hls_playlist.m3u8")
            .webm("https://ksr-video.imgix.net/projects/3275127/video-865539-hls_playlist.m3u8")
            .build()

        assertEquals(video.base(), "https://www.kickstarter.com/project/base.mp4")
        assertEquals(video.frame(), "https://www.kickstarter.com/project/frame.mp4")
        assertEquals(video.high(), "https://www.kickstarter.com/project/high.mp4")
        assertEquals(video.hls(), "https://ksr-video.imgix.net/projects/3275127/video-865539-hls_playlist.m3u8")
        assertEquals(video.webm(), "https://ksr-video.imgix.net/projects/3275127/video-865539-hls_playlist.m3u8")
    }

    @Test
    fun testDefaultToBuilder() {
        val video = Video.builder().build().toBuilder().base("https://www.kickstarter.com/project/base.mp4").build()
        assertEquals(video.base(), "https://www.kickstarter.com/project/base.mp4")
    }

    @Test
    fun testVideo_equalFalse() {
        val video = Video.builder().build()
        val video2 = VideoFactory.hlsVideo()
        val video3 = VideoFactory.video()
        val video4 = Video.builder().webm("https://ksr-video.imgix.net/projects/3275127/video-865539-hls_playlist.m3u8").build()

        assertFalse(video == video2)
        assertFalse(video == video3)
        assertFalse(video == video4)

        assertFalse(video3 == video2)
        assertFalse(video3 == video4)
    }

    @Test
    fun testVideo_equalTrue() {
        val video1 = Video.builder().build()
        val video2 = Video.builder().build()

        assertEquals(video1, video2)
    }
}
