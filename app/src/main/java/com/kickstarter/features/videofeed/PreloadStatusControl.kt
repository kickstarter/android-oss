package com.kickstarter.features.videofeed

import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager.PreloadStatus.specifiedRangeLoaded
import androidx.media3.exoplayer.source.preload.TargetPreloadStatusControl


@androidx.media3.common.util.UnstableApi
class PreloadStatusControl(private val currentPlayingIndexProvider: () -> Int) : TargetPreloadStatusControl<Int, DefaultPreloadManager.PreloadStatus> {
    override fun getTargetPreloadStatus(rankingData: Int): DefaultPreloadManager.PreloadStatus {
        val currentIdx = currentPlayingIndexProvider()
        val distance = rankingData - currentIdx

        return when {
            distance == 0 -> DefaultPreloadManager.PreloadStatus.PRELOAD_STATUS_NOT_PRELOADED
            distance == 1 -> specifiedRangeLoaded(5_000_000L) // Preload 5s
            distance <= 3 -> DefaultPreloadManager.PreloadStatus.PRELOAD_STATUS_SOURCE_PREPARED
            else -> DefaultPreloadManager.PreloadStatus.PRELOAD_STATUS_NOT_PRELOADED // Too far away, don't load
        }
    }
}