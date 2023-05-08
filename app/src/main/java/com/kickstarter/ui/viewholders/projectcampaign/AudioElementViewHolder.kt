package com.kickstarter.ui.viewholders.projectcampaign

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.SeekBar
import com.kickstarter.R
import com.kickstarter.databinding.ViewElementAudioFromHtmlBinding
import com.kickstarter.libs.KSLifecycleEvent
import com.kickstarter.libs.htmlparser.AudioViewElement
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.ui.viewholders.KSViewHolder
import com.kickstarter.viewmodels.projectpage.AudioViewElementViewHolderViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class AudioElementViewHolder(
    private val binding: ViewElementAudioFromHtmlBinding,
    lifecycleBehaviorSubject: BehaviorSubject<KSLifecycleEvent>
) : KSViewHolder(binding.root) {

    private val viewModel = AudioViewElementViewHolderViewModel.AudioViewElementViewHolderViewModel(lifecycleBehaviorSubject)
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private var isPrepared = false
    private val updateObservable = io.reactivex.Observable.interval(500, TimeUnit.MILLISECONDS)
    private lateinit var updateDisposable: Disposable
    private val disposables = CompositeDisposable()

    init {
        this.viewModel.outputs.preparePlayerWithUrl()
            .subscribe {
                initializePlayer(it)
            }.addToDisposable(disposables)

        this.viewModel.outputs.stopPlayer()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                stopPlayer()
            }.addToDisposable(disposables)

        this.viewModel.pausePlayer()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                pausePlayer()
            }.addToDisposable(disposables)

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
        disposables.dispose()
        super.destroy()
    }

    fun togglePlayerState() {
        if (!isPrepared) return
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

            updateDisposable = updateObservable
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
            updateDisposable.dispose()
            mediaPlayer.pause()
        }
    }

    fun stopPlayer() {
        if (isPrepared && mediaPlayer.isPlaying) {
            updateDisposable.dispose()
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
