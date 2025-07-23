package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.kickstarter.R
import com.kickstarter.databinding.VideoPlayerLayoutBinding
import com.kickstarter.libs.Build
import com.kickstarter.libs.utils.WebUtils.userAgent
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.VideoViewModel.Factory
import com.kickstarter.viewmodels.VideoViewModel.VideoViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

@UnstableApi
class VideoActivity : AppCompatActivity() {
    private lateinit var build: Build
    private var player: ExoPlayer? = null
    private var playerPosition: Long? = null
    private var trackSelector: DefaultTrackSelector? = null
    private lateinit var binding: VideoPlayerLayoutBinding

    private lateinit var viewModelFactory: Factory
    private val viewModel: VideoViewModel by viewModels { viewModelFactory }

    private var disposables = CompositeDisposable()

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            viewModel.inputs.resume()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = VideoPlayerLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)

        val environment = this.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env, intent = intent)
            env
        }

        build = requireNotNull(environment?.build())

        val fullscreenButton: ImageView = binding.playerView.findViewById(R.id.exo_fullscreen_icon)
        fullscreenButton.setImageResource(R.drawable.ic_fullscreen_close)

        fullscreenButton.setOnClickListener {
            back()
        }

        viewModel.outputs.preparePlayerWithUrl()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { preparePlayer(it) }
            .addToDisposable(disposables)

        viewModel.outputs.preparePlayerWithUrlAndPosition()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                playerPosition = it.second
                preparePlayer(it.first)
            }
            .addToDisposable(disposables)

        lifecycle.addObserver(lifecycleObserver)

        this.onBackPressedDispatcher.addCallback {
            back()
        }

        setUpConnectivityStatusCheck(lifecycle)
    }

    public override fun onDestroy() {
        super.onDestroy()
        player = null
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

    private fun back() {
        val intent = Intent()
            .putExtra(IntentKey.VIDEO_SEEK_POSITION, player?.currentPosition)
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

    @OptIn(UnstableApi::class)
    private fun preparePlayer(videoUrl: String) {
        val adaptiveTrackSelectionFactory: AdaptiveTrackSelection.Factory = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(this, adaptiveTrackSelectionFactory)
        trackSelector?.let {
            ExoPlayer.Builder(this).setTrackSelector(it)
        }

        val playerBuilder = ExoPlayer.Builder(this)
        trackSelector?.let { playerBuilder.setTrackSelector(it) }
        player = playerBuilder.build()

        binding.playerView.player = player
        player?.addListener(eventListener)

        player?.setMediaSource(getMediaSource(videoUrl))
        player?.prepare()
        playerPosition?.let {
            player?.seekTo(it)
        }
        player?.playWhenReady = true
    }

    @OptIn(UnstableApi::class)
    private fun getMediaSource(videoUrl: String): MediaSource {
        val dataSourceFactory = DefaultHttpDataSource.Factory().setUserAgent(userAgent(build))
        val videoUri = videoUrl.toUri()
        val fileType = Util.inferContentType(videoUri)

        return if (fileType == C.TYPE_HLS) {
            // HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaBrowserCompat.MediaItem.fromUri(videoUri))
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(videoUri))
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(videoUri))
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

    private val eventListener: Player.Listener = @UnstableApi
    object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            onStateChanged(playbackState)
        }
    }
}
