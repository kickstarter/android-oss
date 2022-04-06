package com.kickstarter.ui.viewholders.projectcampaign

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.SeekBar
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
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private var isPrepared = false
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
                initializePlayer(it)
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
            togglePlayerState()
        }

        this.mediaPlayer.setOnCompletionListener {
            resetPlayer()
        }

        this.mediaPlayer.setOnPreparedListener {
            isPrepared = true
            prepareMediaPlayer()
        }

        this.binding.progressbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress * 1000)
                        updateProgressTextLabel()
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    togglePlayerState()
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    togglePlayerState()
                }
            })
    }

    override fun destroy() {
        mediaPlayer.release()
        super.destroy()
    }

    fun togglePlayerState() {
        if (mediaPlayer.isPlaying) {
            pausePlayer()
        } else {
            startPlayer()
        }
    }

    fun resetPlayer() {
        if (isPrepared) {
            stopPlayer()
            this.binding.progressbar.progress = 0
            this.binding.playPause.setImageResource(R.drawable.exo_controls_play)
            mediaPlayer.seekTo(0)
            updateProgressTextLabel()
            prepareMediaPlayer()
        }
    }

    fun startPlayer() {
        if (isPrepared && !mediaPlayer.isPlaying) {
            this.binding.playPause.setImageResource(R.drawable.exo_controls_pause)
            mediaPlayer.start()

            disposable = updateObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    updateProgressUI()
                }
        }
    }

    fun updateProgressUI() {
        if (isPrepared && mediaPlayer.isPlaying) {
            updateProgressTextLabel()

            val currentPosition = mediaPlayer.currentPosition / 1000
            this.binding.progressbar.progress = currentPosition
        }
    }

    fun pausePlayer() {
        if (isPrepared && mediaPlayer.isPlaying) {
            this.binding.playPause.setImageResource(R.drawable.exo_controls_play)
            disposable.dispose()
            mediaPlayer.pause()
        }
    }

    fun stopPlayer() {
        if (isPrepared && mediaPlayer.isPlaying) {
            disposable.dispose()
            mediaPlayer.stop()
        }
    }

    fun initializePlayer(url: String) {
        try {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            isPrepared = false
            mediaPlayer.release()
        }
    }

    fun prepareMediaPlayer() {
        if (isPrepared) {
            val duration = mediaPlayer.duration.toLong()
            this.binding.duration.text = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(duration)
                )
            )

            updateProgressTextLabel()
            this.binding.progressbar.max = mediaPlayer.duration / 1000
        }
    }

    fun updateProgressTextLabel() {
        if (isPrepared) {
            val currentPos = mediaPlayer.currentPosition.toLong()
            val progress = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currentPos),
                TimeUnit.MILLISECONDS.toSeconds(currentPos) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(currentPos)
                )
            )
            this.binding.progress.text = progress
        }
    }

    override fun bindData(data: Any?) {
        (data as? AudioViewElement)?.run {
            viewModel.inputs.configureWith(this)
        }
    }
}
