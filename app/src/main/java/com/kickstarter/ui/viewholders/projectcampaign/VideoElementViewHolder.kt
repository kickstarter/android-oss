package com.kickstarter.ui.viewholders.projectcampaign

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.kickstarter.R
import com.kickstarter.databinding.ViewElementVideoFromHtmlBinding
import com.kickstarter.libs.Build
import com.kickstarter.libs.htmlparser.VideoViewElement
import com.kickstarter.libs.utils.WebUtils
import com.kickstarter.ui.extensions.loadImage
import com.kickstarter.ui.viewholders.KSViewHolder

class VideoElementViewHolder(
    val binding: ViewElementVideoFromHtmlBinding,
    val requireActivity: FragmentActivity
) : KSViewHolder(binding.root) {

    private lateinit var build: Build

    private val thumbnail = binding.thumbnail
    private val loadingIndicator = binding.loadingIndicator
    private val videoPlayerView = binding.videoPlayerView

    private var parentViewGroup: ViewGroup? = null
    private var fullscreenButton: ImageView? = null
    private var mExoPlayerFullscreen = false
    private var trackSelector: DefaultTrackSelector? = null

    private var originalOrientation = requireActivity.requestedOrientation
    private var originalSystemUiVisibility = requireActivity.window.decorView.systemUiVisibility

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_BUFFERING) {

                // Buffering..
                // set progress bar visible here
                // set thumbnail visible
                thumbnail.visibility = View.VISIBLE
                loadingIndicator.visibility = View.VISIBLE
            }

            if (playbackState == Player.STATE_READY) {
                // [PlayerView] has fetched the video duration so this is the block to hide the buffering progress bar
                loadingIndicator.visibility = View.GONE
                // set thumbnail gone
                thumbnail.visibility = View.GONE
            }
        }
    }

    fun configure(element: VideoViewElement) {
        build = environment().build()
        thumbnail.loadImage(element.thumbnailUrl, context())
        loadVideo(element.sourceUrl)
        fullscreenButton = videoPlayerView.findViewById(R.id.exo_fullscreen_icon)

        fullscreenButton?.setOnClickListener {
            if (!mExoPlayerFullscreen)
                openFullscreenDialog()
            else
                closeFullscreenDialog()
        }
    }

    override fun bindData(data: Any?) {
        (data as? VideoViewElement).apply {
            this?.let { configure(it) }
        }
    }

    private fun loadVideo(url: String) {
        val adaptiveTrackSelectionFactory: AdaptiveTrackSelection.Factory = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(context(), adaptiveTrackSelectionFactory)

        val playerBuilder = SimpleExoPlayer.Builder(context())
        trackSelector?.let { playerBuilder.setTrackSelector(it) }

        // Provide url to load the video from here
        val player = playerBuilder.build().also {
            it.playWhenReady = false
            it.prepare(getMediaSource(url))
        }

        // add player with its index to map
        if (playersMap.containsKey(bindingAdapterPosition)) {

            playersMap[bindingAdapterPosition]?.currentPosition?.let {
                player.seekTo(it)
                if (it != 0L)
                    player.playWhenReady = true
            }
        }

        playersMap[bindingAdapterPosition] = player

        videoPlayerView.apply {
            // When changing track, retain the latest frame instead of showing a black screen
            this.setKeepContentOnPlayerReset(true)
            // We'll show the controller, change to true if want controllers as pause and start
            useController = true
            this.player = player
        }

        videoPlayerView.player?.addListener(listener)
    }

    private fun getMediaSource(videoUrl: String): MediaSource {
        val dataSourceFactory = DefaultHttpDataSource.Factory().setUserAgent(
            WebUtils.userAgent(
                build
            )
        )
        val videoUri = Uri.parse(videoUrl)
        val fileType = Util.inferContentType(videoUri)

        return if (fileType == C.TYPE_HLS) {
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri)
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri)
        }
    }

    private val mFullScreenDialog: Dialog =
        object : Dialog(context(), android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            override fun onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog()
                super.onBackPressed()
            }
        }

    private fun openFullscreenDialog() {
        parentViewGroup = (videoPlayerView.parent as ViewGroup)
        parentViewGroup?.removeView(videoPlayerView)
        mFullScreenDialog.addContentView(
            videoPlayerView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        fullscreenButton?.setImageDrawable(
            ContextCompat.getDrawable(
                context(),
                R.drawable.ic_fullscreen_close
            )
        )
        mExoPlayerFullscreen = true
        originalSystemUiVisibility = requireActivity.window.decorView.systemUiVisibility
        originalOrientation = requireActivity.requestedOrientation
        requireActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        requireActivity.window.decorView.systemUiVisibility = 3846
        mFullScreenDialog.show()
    }

    private fun closeFullscreenDialog() {
        (videoPlayerView.parent as ViewGroup).removeView(videoPlayerView)
        parentViewGroup?.addView(videoPlayerView)
        mExoPlayerFullscreen = false
        requireActivity.window.decorView.systemUiVisibility = originalSystemUiVisibility
        requireActivity.requestedOrientation = originalOrientation
        mFullScreenDialog.dismiss()
        fullscreenButton?.setImageDrawable(
            ContextCompat.getDrawable(
                context(),
                R.drawable.ic_fullscreen_open
            )
        )
    }

    fun releasePlayer(index: Int) {
        playersMap[index]?.let {
            it.removeListener(listener)
            it.release()
            trackSelector = null
        }
    }
    
    companion object {
        // for hold all players generated
        private var playersMap: MutableMap<Int, SimpleExoPlayer?> = mutableMapOf()

        // for hold current player
        private var currentPlayingVideo: Pair<Int, SimpleExoPlayer?>? = null

        fun releaseAllPlayers() {
            playersMap.onEachIndexed { index, item ->
                item.value?.release()
                playersMap[index] = null
            }
            playersMap.clear()
            playersMap = mutableMapOf()
            currentPlayingVideo?.second?.release()
            currentPlayingVideo = null
        }

        fun releasePlayersOnPause() {
            playersMap.forEach { item ->
                item.value?.playWhenReady = false
            }
        }

        // call when scroll to pause any playing player
        private fun pauseCurrentPlayingVideo() {
            if (currentPlayingVideo != null) {
                currentPlayingVideo?.second?.playWhenReady = false
            }
        }

        fun playIndexThenPausePreviousPlayer(index: Int) {
            if (playersMap[index]?.playWhenReady == false) {
                pauseCurrentPlayingVideo()
                playersMap[index]?.currentPosition?.let {
                    playersMap[index]?.isCurrentWindowSeekable
                    if (it != 0L)
                        playersMap[index]?.playWhenReady = true
                }

                currentPlayingVideo = Pair(index, playersMap[index])
            }
        }
    }
}
