package com.kickstarter.ui.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
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
    private var trackSelector: DefaultTrackSelector? = null

    private var playWhenReady = false
    private var currentItem = 0
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
        if (element == null)
            return
        val adaptiveTrackSelectionFactory: AdaptiveTrackSelection.Factory = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(context, adaptiveTrackSelectionFactory)

        fullscreenButton = videoPlayerView.findViewById(R.id.exo_fullscreen_icon)

        player = ExoPlayer.Builder(context).apply {
            trackSelector?.let { setTrackSelector(it) }
        }.build()
            .also { exoPlayer ->
                videoPlayerView.player = exoPlayer
                element?.sourceUrl?.let {
                    exoPlayer.setMediaItem(MediaItem.fromUri(it))
                    val playerIsResuming = (playbackPosition != 0L)
                    exoPlayer.setMediaSource(getMediaSource(it), playerIsResuming)
                }
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.seekTo(playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
            }

        videoPlayerView.player = player

        fullscreenButton?.setOnClickListener {
            element?.sourceUrl?.let { url ->
                onFullScreenOpenedClickedListener?.onFullScreenViewClicked(this, url, player?.currentPosition ?: 0)
            }
        }
    }

    fun setFullscreenButtonDrawableResource(closeFullScreen: Boolean = false) {
        fullscreenButton?.setImageResource(R.drawable.ic_fullscreen_open)
        if (closeFullScreen)
            fullscreenButton?.setImageResource(R.drawable.ic_fullscreen_close)
    }

    private fun getMediaSource(videoUrl: String): MediaSource {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
        val videoUri = Uri.parse(videoUrl)
        val fileType = Util.inferContentType(videoUri)

        return if (fileType == C.TYPE_HLS) {
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(videoUri))
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUri))
        }
    }

    fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player = null
    }

    fun setOnFullScreenClickedListener(onFullScreenOpenedClickedListener: OnFullScreenOpenedClickedListener) {
        this.onFullScreenOpenedClickedListener = onFullScreenOpenedClickedListener
    }

    fun pausePlayer() {
        player?.pause()
    }
}

interface OnFullScreenOpenedClickedListener {
    fun onFullScreenViewClicked(view: View, url: String, seekPosition: Long = 0L)
}
