package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Build.VERSION
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.kickstarter.R
import com.kickstarter.databinding.VideoPlayerLayoutBinding
import com.kickstarter.libs.Build
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.VideoViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class VideoActivity : AppCompatActivity() {

    private lateinit var build: Build
    private lateinit var binding: VideoPlayerLayoutBinding
    private lateinit var viewModelFactory: VideoViewModel.Factory
    private val viewModel: VideoViewModel.VideoViewModel by viewModels { viewModelFactory }

    private lateinit var player: ExoPlayer
    private var playerPosition: Long? = null
    private val disposables = CompositeDisposable()

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            viewModel.inputs.resume()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = VideoPlayerLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(window, binding.root)
        setContentView(binding.root)

        val environment = this.getEnvironment()?.let { env ->
            viewModelFactory = VideoViewModel.Factory(env, intent = intent)
            env
        }
        build = requireNotNull(environment?.build())

        player = ExoPlayer.Builder(this)
            .build()

        binding.playerView.findViewById<ImageView>(R.id.exo_fullscreen_icon).apply {
            setImageResource(R.drawable.ic_fullscreen_close)
            setOnClickListener { back() }
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

        onBackPressedDispatcher.addCallback { back() }

        setUpConnectivityStatusCheck(lifecycle)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            if (VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                enterImmersiveModeApi30()
            } else {
                systemUIFlags()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onDestroy() {
        releasePlayer()
        disposables.clear()
        super.onDestroy()
    }

    private fun preparePlayer(videoUrl: String) {
        val mediaItem = MediaItem.Builder()
            .setUri(videoUrl)
            .build()

        player.also {
            binding.playerView.player = it
            it.addListener(eventListener)
            it.setMediaItem(mediaItem)
            it.prepare()
            playerPosition?.let(it::seekTo)
            it.playWhenReady = true
        }
    }

    private fun releasePlayer() {
        player.also {
            playerPosition = it.currentPosition
            it.duration.takeIf { duration -> duration > 0 }?.let { duration ->
                viewModel.inputs.onVideoCompleted(duration, playerPosition ?: 0L)
            }
            it.removeListener(eventListener)
            it.release()
        }
    }

    private fun back() {
        val intent = Intent().putExtra(IntentKey.VIDEO_SEEK_POSITION, player.currentPosition)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    // For API 30+ (Android 11 and above)
    @RequiresApi(android.os.Build.VERSION_CODES.R)
    private fun enterImmersiveModeApi30() {
        window.insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
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
        when (playbackState) {
            Player.STATE_READY -> {
                player.duration.let {
                    viewModel.inputs.onVideoStarted(it, playerPosition ?: 0L)
                }
                binding.loadingIndicator.visibility = View.GONE
            }

            Player.STATE_ENDED -> finish()

            Player.STATE_BUFFERING -> binding.loadingIndicator.visibility = View.VISIBLE

            else -> binding.loadingIndicator.visibility = View.GONE
        }
    }

    private val eventListener = @UnstableApi
    object : Player.Listener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            onStateChanged(playbackState)
        }
    }
}
