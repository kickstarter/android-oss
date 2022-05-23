package com.kickstarter.ui.viewholders.projectcampaign

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.kickstarter.R
import com.kickstarter.databinding.ViewElementVideoFromHtmlBinding
import com.kickstarter.libs.Build
import com.kickstarter.libs.htmlparser.VideoViewElement
import com.kickstarter.ui.adapters.projectcampaign.ViewElementAdapter
import com.kickstarter.ui.extensions.loadImage
import com.kickstarter.ui.viewholders.KSViewHolder

class VideoElementViewHolder(
    val binding: ViewElementVideoFromHtmlBinding,
    private val fullScreenDelegate: ViewElementAdapter.FullScreenDelegate,
    val requireActivity: FragmentActivity
) : KSViewHolder(binding.root) {

    private lateinit var build: Build

    private val thumbnail = binding.thumbnail
    private val loadingIndicator = binding.loadingIndicator
    private val videoPlayerView = binding.videoPlayerView

    private var fullscreenButton: ImageView? = null
    private var trackSelector: DefaultTrackSelector? = null

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

        override fun onIsLoadingChanged(isLoading: Boolean) {
            super.onIsLoadingChanged(isLoading)
            loadingIndicator.isVisible = isLoading
            thumbnail.isVisible = isLoading
        }
    }

    fun configure(element: VideoViewElement) {
        build = requireNotNull(environment().build())
        thumbnail.loadImage(element.thumbnailUrl, context())
        loadVideo(element.sourceUrl, element.seekPosition)
        fullscreenButton = videoPlayerView.findViewById(R.id.exo_fullscreen_icon)

        fullscreenButton?.setOnClickListener {
            openFullscreenDialog(element.sourceUrl)
        }
    }

    override fun bindData(data: Any?) {
        (data as? VideoViewElement).apply {
            this?.let { configure(it) }
        }
    }

    private fun loadVideo(url: String, seekPosition: Long) {
        val adaptiveTrackSelectionFactory: AdaptiveTrackSelection.Factory = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(context(), adaptiveTrackSelectionFactory)

        val playerBuilder = SimpleExoPlayer.Builder(context())
        trackSelector?.let { playerBuilder.setTrackSelector(it) }

        val playerIsResuming = (seekPosition != 0L || playersMap[bindingAdapterPosition]?.currentPosition != 0L)

        // Provide url to load the video from here
        val player = playerBuilder.build().also { exoPlayer ->
            exoPlayer.playWhenReady = false
            if (seekPosition != 0L) {
                exoPlayer.seekTo(seekPosition)
                exoPlayer.playWhenReady = true
            } else {
                // add player with its index to map
                if (playersMap.containsKey(bindingAdapterPosition)) {
                    playersMap[bindingAdapterPosition]?.currentPosition?.let {
                        exoPlayer.seekTo(it)
                        if (it != 0L)
                            exoPlayer.playWhenReady = true
                    }
                }
            }
            exoPlayer.setMediaItem(MediaItem.fromUri(url), !playerIsResuming)
            exoPlayer.prepare()
        }

        videoPlayerView.apply {
            // When changing track, retain the latest frame instead of showing a black screen
            this.setKeepContentOnPlayerReset(true)
            // We'll show the controller, change to true if want controllers as pause and start
            useController = true
            this.player = player
            this.player?.addListener(listener)
        }

        playersMap[bindingAdapterPosition] = player
    }

    private fun openFullscreenDialog(url: String) {
        fullScreenDelegate.onFullScreenOpened(
            bindingAdapterPosition, url,
            playersMap[bindingAdapterPosition]?.currentPosition ?: 0
        )
    }

    fun releasePlayer(index: Int) {
        playersMap[index]?.let {
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
            val itr = playersMap.iterator()
            while (itr.hasNext()) {
                val entry = itr.next()

                entry.value?.release()
                entry.setValue(null)
                itr.remove()
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

        fun setPlayerSeekPosition(index: Int, seekPosition: Long) {
            playersMap[index]?.seekTo(seekPosition)
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
                    if (it != 0L) {
                        playersMap[index]?.playWhenReady = true
                    }
                }

                currentPlayingVideo = Pair(index, playersMap[index])
            }
        }
    }
}
