package com.kickstarter.ui.data

data class MediaElement(
    val videoModelElement: VideoModelElement?,
    val thumbnailUrl: String?
)

data class VideoModelElement(
    val sourceUrl: String?,
    val seekPosition: Long = 0L
)
