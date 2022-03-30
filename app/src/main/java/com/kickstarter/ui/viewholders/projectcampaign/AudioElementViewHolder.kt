package com.kickstarter.ui.viewholders.projectcampaign

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.kickstarter.databinding.ViewElementAudioFromHtmlBinding
import com.kickstarter.libs.htmlparser.AudioViewElement
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.viewmodels.projectpage.AudioViewElementViewHolderViewModel
import com.trello.rxlifecycle.FragmentEvent
import rx.Observable

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
    }

    private fun startPlayer() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    private fun stopPlayer() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    private fun prepareMediaPlayer(url: String) {
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepare()
        // mediaPlayer.start() // TODO remove once the play button UI is ready
    }

    override fun bindData(data: Any?) {
        (data as? AudioViewElement)?.run {
            viewModel.inputs.configureWith(this)
        }
    }
}
