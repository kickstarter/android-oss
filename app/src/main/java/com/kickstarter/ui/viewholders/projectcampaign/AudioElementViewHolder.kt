package com.kickstarter.ui.viewholders.projectcampaign

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.kickstarter.R
import com.kickstarter.databinding.ViewElementAudioFromHtmlBinding
import com.kickstarter.libs.htmlparser.AudioViewElement
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.viewmodels.projectpage.AudioViewElementViewHolderViewModel
import com.trello.rxlifecycle.FragmentEvent
import rx.Observable
import java.lang.Exception

class AudioElementViewHolder(
    private val binding: ViewElementAudioFromHtmlBinding,
    private val lifecycle: Observable<FragmentEvent>
) : KSViewHolder(binding.root) {

    private val viewModel = AudioViewElementViewHolderViewModel.ViewModel(environment())
    private val mediaPlayer: MediaPlayer by lazy { MediaPlayer() }

    init {
        this.lifecycle
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .filter { ObjectUtils.isNotNull(it) }
            .subscribe {
                this.viewModel.inputs.fragmentLifeCycle(it)
            }

        this.viewModel.outputs.preparePlayerWithUrl()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                prepareMediaPlayer(it)
            }

        this.viewModel.outputs.startPlayer()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                startPlayer()
            }

        this.viewModel.outputs.stopPlayer()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                stopPlayer()
            }

        this.binding.playPause.setOnClickListener {
            toggleButton()
        }
    }

    private fun toggleButton() {
        if (mediaPlayer.isPlaying) {
            this.binding.playPause.setImageResource(R.drawable.exo_controls_pause)
            pausePlayer()
        } else {
            this.binding.playPause.setImageResource(R.drawable.exo_controls_play)
            startPlayer()
        }
    }

    private fun startPlayer() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    private fun pausePlayer() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    private fun stopPlayer() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    private fun prepareMediaPlayer(url: String) {
        try {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepare()

            this.binding.duration.text = mediaPlayer.duration.toString()
        } catch (e: Exception) {
        }
    }

    override fun bindData(data: Any?) {
        (data as? AudioViewElement)?.run {
            viewModel.inputs.configureWith(this)
        }
    }
}
