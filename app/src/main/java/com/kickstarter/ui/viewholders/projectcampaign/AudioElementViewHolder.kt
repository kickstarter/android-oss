package com.kickstarter.ui.viewholders.projectcampaign

import android.content.res.ColorStateList
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Build
import com.kickstarter.R
import com.kickstarter.databinding.ViewElementAudioFromHtmlBinding
import com.kickstarter.libs.htmlparser.AudioViewElement
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.viewmodels.projectpage.AudioViewElementViewHolderViewModel
import com.trello.rxlifecycle.FragmentEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import rx.Observable
import java.util.concurrent.TimeUnit

class AudioElementViewHolder(
    private val binding: ViewElementAudioFromHtmlBinding,
    private val lifecycle: Observable<FragmentEvent>
) : KSViewHolder(binding.root) {

    private val viewModel = AudioViewElementViewHolderViewModel.ViewModel(environment())
    private val mediaPlayer: MediaPlayer by lazy { MediaPlayer() }
    private var url = ""
    private val updateObservable = io.reactivex.Observable.interval(500, TimeUnit.MILLISECONDS)
    private lateinit var disposable: Disposable

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
                url = it
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

        this.viewModel.pausePlayer()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                pausePlayer()
            }

        this.binding.playPause.setOnClickListener {
            toggleButton()
        }

        mediaPlayer.setOnCompletionListener(
            OnCompletionListener {
                resetPlayer()
            }
        )
    }

    private fun toggleButton() {
        if (mediaPlayer.isPlaying) {
            this.binding.playPause.setImageResource(R.drawable.exo_controls_play)
            pausePlayer()
        } else {
            this.binding.playPause.setImageResource(R.drawable.exo_controls_pause)
            startPlayer()
        }
    }

    private fun resetPlayer() {
        this.binding.progressbar.progress = 0
        stopPlayer()
        mediaPlayer.reset()
        disposable.dispose()
        prepareMediaPlayer(url)
    }

    private fun startPlayer() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()

            disposable = updateObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    updateProgressUI()
                }
        }
    }

    private fun updateProgressUI() {
        if (mediaPlayer.isPlaying) {
            val progress = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.currentPosition.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.currentPosition.toLong()) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            mediaPlayer.currentPosition.toLong()
                        )
                    )
            )

            val progressOnBAr =
                (mediaPlayer.currentPosition.toDouble() / mediaPlayer.duration.toDouble() * 100).toInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.binding.progressbar.setProgress(progressOnBAr, true)
            } else this.binding.progressbar.progress = progressOnBAr

            this.binding.progressbar.progressTintList = ColorStateList.valueOf(Color.WHITE)
            this.binding.progress.text = progress
        }
    }

    private fun pausePlayer() {
        if (mediaPlayer.isPlaying) {
            disposable.dispose()
            mediaPlayer.pause()
        }
    }

    private fun stopPlayer() {
        if (mediaPlayer.isPlaying) {
            disposable.dispose()
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

            this.binding.duration.text = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.duration.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.duration.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.duration.toLong()))
            )

            val progress = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.currentPosition.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.currentPosition.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.currentPosition.toLong()))
            )
            this.binding.progress.text = progress
        } catch (e: Exception) {
        }
    }

    override fun bindData(data: Any?) {
        (data as? AudioViewElement)?.run {
            viewModel.inputs.configureWith(this)
        }
    }
}
