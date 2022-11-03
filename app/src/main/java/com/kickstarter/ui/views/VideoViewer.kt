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
import com.kickstarter.databinding.VideoViewLayoutBinding
import com.kickstarter.libs.htmlparser.VideoViewElement

class VideoViewer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: VideoViewLayoutBinding =
        VideoViewLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    private val loadingIndicator = binding.loadingIndicator
    private val videoPlayerView = binding.playerView
    private var element: VideoViewElement? = null

    private var player: ExoPlayer? = null

    private var fullscreenButton: ImageView? = null
    private var trackSelector: DefaultTrackSelector? = null

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    private var onFullScreenClickedListener: OnFullScreenClickedListener? = null

    private val playbackStateListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            if (playbackState == Player.STATE_BUFFERING) {

                // Buffering..
                // set progress bar visible here
                // set thumbnail visible
                // thumbnail.visibility = VISIBLE
                loadingIndicator.visibility = VISIBLE
            }

            if (playbackState == Player.STATE_READY) {
                // [PlayerView] has fetched the video duration so this is the block to hide the buffering progress bar
                loadingIndicator.visibility = GONE
            }
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            super.onIsLoadingChanged(isLoading)
            loadingIndicator.isVisible = isLoading
        }
    }

    fun setVideoViewElement(element: VideoViewElement) {
        this.element = element
        initializePlayer()
    }

    fun setPlayerSeekPosition(seekPosition: Long) {
        videoPlayerView.player?.seekTo(seekPosition)
        playbackPosition = seekPosition
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

                playbackPosition?.let {
                    exoPlayer.seekTo(it)
                }
                exoPlayer.playWhenReady = true
                exoPlayer.prepare()
            }

        videoPlayerView.player = player

        fullscreenButton?.setOnClickListener {
            element?.sourceUrl?.let { url ->
                onFullScreenClickedListener?.onFullScreenOpenedViewClicked(this, url, player?.currentPosition ?: 0)
            }
        }
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

    fun setOnFullScreenClickedListener(onFullScreenClickedListener: OnFullScreenClickedListener) {
        this.onFullScreenClickedListener = onFullScreenClickedListener
    }

    fun pausePlayer() {
        player?.pause()
    }
}

interface OnFullScreenClickedListener {
    fun onFullScreenOpenedViewClicked(view: View, url: String, seekPosition: Long = 0L)
}
