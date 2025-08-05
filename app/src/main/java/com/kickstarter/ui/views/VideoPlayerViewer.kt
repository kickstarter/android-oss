package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.kickstarter.R
import com.kickstarter.databinding.VideoPlayerLayoutBinding
import com.kickstarter.libs.utils.extensions.initializeExoplayer
import com.kickstarter.ui.data.VideoModelElement

class VideoPlayerViewer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: VideoPlayerLayoutBinding =
        VideoPlayerLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    private val loadingIndicator = binding.loadingIndicator
    private val videoPlayerView = binding.playerView
    private var element: VideoModelElement? = null

    private var player: ExoPlayer? = null
    private var fullscreenButton: ImageView? = null

    private var playWhenReady = false
    private var playbackPosition = 0L

    private var onFullScreenOpenedClickedListener: OnFullScreenOpenedClickedListener? = null

    private val playbackStateListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            if (playbackState == Player.STATE_BUFFERING) {
                loadingIndicator.visibility = VISIBLE
            }

            if (playbackState == Player.STATE_READY) {
                loadingIndicator.visibility = GONE
            }
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            super.onIsLoadingChanged(isLoading)
            loadingIndicator.isVisible = isLoading
        }
    }

    fun setVideoModelElement(element: VideoModelElement) {
        this.element = element
    }

    fun setPlayerSeekPosition(seekPosition: Long) {
        videoPlayerView.player?.seekTo(seekPosition)
        playbackPosition = seekPosition
    }

    fun setPlayerPlayWhenReadyFlag(playWhenReady: Boolean) {
        if (playWhenReady)
            initializePlayer()
    }

    @OptIn(UnstableApi::class)
    fun initializePlayer() {
        if (element == null || element?.sourceUrl == null) return
        fullscreenButton = videoPlayerView.findViewById(R.id.exo_fullscreen_icon)

        player = context.initializeExoplayer()

        val mediaItem = MediaItem.Builder()
            .setUri(element?.sourceUrl ?: "")
            .build()

        player?.setMediaItem(mediaItem)

        binding.playerView.player = player
        player?.addListener(playbackStateListener)
        player?.prepare()
        element?.seekPosition?.let {
            player?.seekTo(it)
        }
        player?.playWhenReady = true

        fullscreenButton?.setOnClickListener {
            element?.sourceUrl?.let { url ->
                onFullScreenOpenedClickedListener?.onFullScreenViewClicked(
                    this,
                    url,
                    player?.currentPosition ?: 0L
                )
            }
        }
    }

    fun releasePlayer() {
        player?.let {
            playbackPosition = it.currentPosition
            playWhenReady = it.playWhenReady
            it.removeListener(playbackStateListener)
            it.release()
        }
        player = null
    }

    fun setOnFullScreenClickedListener(listener: OnFullScreenOpenedClickedListener) {
        this.onFullScreenOpenedClickedListener = listener
    }

    fun pausePlayer() {
        player?.pause()
    }
}

interface OnFullScreenOpenedClickedListener {
    fun onFullScreenViewClicked(view: View, url: String, seekPosition: Long = 0L)
}
