package com.kickstarter.ui.viewholders.projectcampaign

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
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
    private var player: SimpleExoPlayer? = null
    private var playerPosition: Long? = null
    private val thumbnail = binding.thumbnail
    private val loadingIndicator = binding.loadingIndicator
    private val videoPlayerView = binding.videoPlayerView
    private var parentViewGroup: ViewGroup? = null
    private var fullscreenButton: ImageView? = null

    private var originalOrientation = requireActivity.requestedOrientation
    private var originalSystemUiVisibility = requireActivity.window.decorView.systemUiVisibility

    fun configure(element: VideoViewElement) {
        build = environment().build()
        thumbnail.loadImage(element.thumbnailUrl, context())
        preparePlayer(element.sourceUrl)
    }

    private fun getMediaSource(videoUrl: String): MediaSource {
        val dataSourceFactory = DefaultHttpDataSource.Factory().setUserAgent(
            WebUtils.userAgent(
                build
            )
        )
        val videoUri = Uri.parse(videoUrl)
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri)
    }

    private fun preparePlayer(videoUrl: String) {

        player = SimpleExoPlayer.Builder(context()).build()

        fullscreenButton = videoPlayerView.findViewById(R.id.exo_fullscreen_icon)

        fullscreenButton?.setOnClickListener {
            if (!mExoPlayerFullscreen)
                openFullscreenDialog()
            else
                closeFullscreenDialog()
        }

        val playerIsResuming = (playerPosition != 0L)
        player?.prepare(getMediaSource(videoUrl), playerIsResuming, false)

        binding.videoPlayerView.player = player
        binding.videoPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

        player?.addListener(eventListener)
    }

    private val eventListener: Player.EventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            onStateChanged(playbackState)
        }
    }

    private fun onStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING) {
            // Buffering..
            // set progress bar visible here
            // set thumbnail visible
            thumbnail.visibility = View.VISIBLE
            loadingIndicator.visibility = View.VISIBLE
        }

        if (playbackState == Player.STATE_READY) {

            loadingIndicator.visibility = View.GONE
            // set thumbnail gone
            thumbnail.visibility = View.GONE
        }

        if (playbackState == Player.STATE_ENDED) {
        }
    }

    override fun bindData(data: Any?) {
        (data as? VideoViewElement).apply {
            this?.let { configure(it) }
        }
    }

    private var mExoPlayerFullscreen = false

    private val mFullScreenDialog: Dialog =
        object : Dialog(context(), android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            override fun onBackPressed() {
                if (mExoPlayerFullscreen) closeFullscreenDialog()
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
}
