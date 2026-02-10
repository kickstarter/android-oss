package com.kickstarter.features.videofeed

import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.source.preload.TargetPreloadStatusControl


@androidx.media3.common.util.UnstableApi
class PreloadStatusControl(private val currentPlayingIndexProvider: () -> Int) :
    TargetPreloadStatusControl<Int, DefaultPreloadManager.PreloadStatus> {

    override fun getTargetPreloadStatus(rankingData: Int): DefaultPreloadManager.PreloadStatus {
        val currentIdx = currentPlayingIndexProvider()
        // Use absolute distance
        val distance = kotlin.math.abs(rankingData - currentIdx)

        return when {
            // - handled by ExoPlayer)
            distance == 0 -> DefaultPreloadManager.PreloadStatus.PRELOAD_STATUS_NOT_PRELOADED

            // - Next/Previous (Buffer 5s)
            distance == 1 -> DefaultPreloadManager.PreloadStatus.specifiedRangeLoaded(5_000_000L)

            // - 2-3 away Prepare Metadata
            distance <= 3 -> DefaultPreloadManager.PreloadStatus.PRELOAD_STATUS_SOURCE_PREPARED

            // - Too far: Unload
            else -> DefaultPreloadManager.PreloadStatus.PRELOAD_STATUS_NOT_PRELOADED
        }
    }
}