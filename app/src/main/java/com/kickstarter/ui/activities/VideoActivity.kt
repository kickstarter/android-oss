package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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
import com.kickstarter.databinding.VideoPlayerLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.Build
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.WebUtils.userAgent
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.VideoViewModel
import com.trello.rxlifecycle.ActivityEvent

@RequiresActivityViewModel(VideoViewModel.ViewModel::class)
class VideoActivity : BaseActivity<VideoViewModel.ViewModel>() {
    private lateinit var build: Build
    private var player: SimpleExoPlayer? = null
    private var playerPosition: Long? = null
    private var trackSelector: DefaultTrackSelector? = null
    private lateinit var binding: VideoPlayerLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = VideoPlayerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        build = environment().build()

        viewModel.outputs.preparePlayerWithUrl()
            .compose(Transformers.takeWhen(lifecycle().filter { other: ActivityEvent? -> ActivityEvent.RESUME.equals(other) }))
            .compose(bindToLifecycle())
            .subscribe { preparePlayer(it) }

        viewModel.outputs.preparePlayerWithUrlAndPosition()
            .compose(Transformers.takeWhen(lifecycle().filter { other: ActivityEvent? -> ActivityEvent.RESUME.equals(other) }))
            .compose(bindToLifecycle())
            .subscribe {
                playerPosition = it.second
                preparePlayer(it.first)
            }
    }

    public override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    public override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            binding.videoPlayerLayout.systemUiVisibility = systemUIFlags()
        }
    }

    override fun back() {
        val intent = Intent()
            .putExtra(IntentKey.VIDEO_SEEK_POSITION, playerPosition)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun systemUIFlags(): Int {
        return (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE
            )
    }

    private fun onStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            player?.duration?.let {
                viewModel.inputs.onVideoStarted(it, playerPosition ?: 0L)
            }
        }

        if (playbackState == Player.STATE_ENDED) {
            finish()
        }

        if (playbackState == Player.STATE_BUFFERING) {
            binding.loadingIndicator.visibility = View.VISIBLE
        } else {
            binding.loadingIndicator.visibility = View.GONE
        }
    }

    private fun preparePlayer(videoUrl: String) {
        val adaptiveTrackSelectionFactory: AdaptiveTrackSelection.Factory = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(this, adaptiveTrackSelectionFactory)
//        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        val player2 = trackSelector?.let {
            SimpleExoPlayer.Builder(this).setTrackSelector(
                it
            )
        }
        val playerBuilder = SimpleExoPlayer.Builder(this)
        trackSelector?.let { playerBuilder.setTrackSelector(it) }
        player = playerBuilder.build()

        binding.playerView.player = player
        player?.addListener(eventListener)

        val playerIsResuming = (playerPosition != 0L)
        player?.prepare(getMediaSource(videoUrl), playerIsResuming, false)
        playerPosition?.let {
            player?.seekTo(it)
        }
        player?.playWhenReady = true
    }

    private fun getMediaSource(videoUrl: String): MediaSource {
        val dataSourceFactory = DefaultHttpDataSource.Factory().setUserAgent(userAgent(build))
        val videoUri = Uri.parse(videoUrl)
        val fileType = Util.inferContentType(videoUri)

        return if (fileType == C.TYPE_HLS) {
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri)
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri)
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            playerPosition = player?.currentPosition
            player?.duration?.let {
                viewModel.inputs.onVideoCompleted(it, playerPosition ?: 0L)
            }
            player?.removeListener(eventListener)
            player?.release()
            trackSelector = null
            player = null
        }
    }

    private val eventListener: Player.EventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            onStateChanged(playbackState)
        }
    }
}
