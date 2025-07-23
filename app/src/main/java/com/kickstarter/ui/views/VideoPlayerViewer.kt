package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.kickstarter.R
import com.kickstarter.databinding.VideoPlayerLayoutBinding
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
            loadingIndicator.isVisible = playbackState == Player.STATE_BUFFERING
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            loadingIndicator.isVisible = isLoading
        }
    }

    fun setVideoModelElement(element: VideoModelElement) {
        this.element = element
        initializePlayer()
    }

    fun setPlayerSeekPosition(seekPosition: Long) {
        videoPlayerView.player?.seekTo(seekPosition)
        playbackPosition = seekPosition
    }

    fun setPlayerPlayWhenReadyFlag(playWhenReady: Boolean) {
        this.playWhenReady = playWhenReady
        videoPlayerView.player?.playWhenReady = this.playWhenReady
    }

    fun initializePlayer() {
        if (element == null) return

        fullscreenButton = videoPlayerView.findViewById(R.id.exo_fullscreen_icon)

        player = ExoPlayer.Builder(context)
            .build()
            .also { exoPlayer ->
                videoPlayerView.player = exoPlayer

                element?.sourceUrl?.let { url ->
                    val mediaItem = MediaItem.Builder()
                        .setUri(url)
                        .build()
                    exoPlayer.setMediaItem(mediaItem)
                }

                exoPlayer.addListener(playbackStateListener)
                exoPlayer.seekTo(playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
            }

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

    fun setFullscreenButtonDrawableResource(closeFullScreen: Boolean = false) {
        fullscreenButton?.setImageResource(
            if (closeFullScreen) R.drawable.ic_fullscreen_close
            else R.drawable.ic_fullscreen_open
        )
    }

    fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
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
