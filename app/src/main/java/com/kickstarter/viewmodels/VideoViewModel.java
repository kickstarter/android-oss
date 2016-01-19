package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.google.android.exoplayer.ExoPlayer;
import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.libs.KSRendererBuilder;
import com.kickstarter.libs.KSVideoPlayer;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Video;
import com.kickstarter.ui.activities.VideoActivity;
import com.kickstarter.viewmodels.errors.VideoViewModelErrors;
import com.kickstarter.viewmodels.inputs.VideoViewModelInputs;
import com.kickstarter.viewmodels.outputs.VideoViewModelOutputs;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class VideoViewModel extends ViewModel<VideoActivity> implements VideoViewModelInputs, VideoViewModelOutputs,
  VideoViewModelErrors, KSVideoPlayer.Listener {

  // INPUTS
  private final BehaviorSubject<MediaController> mediaControllerBehaviorSubject = BehaviorSubject.create();
  private final PublishSubject<List<Object>> playerNeedsPrepare = PublishSubject.create();
  private final PublishSubject<KSVideoPlayer> playerNeedsRelease = PublishSubject.create();
  private final PublishSubject<Void> videoEnded = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Integer> playbackState = PublishSubject.create();
  public Observable<Integer> playbackState() {
    return playbackState;
  }
  private final PublishSubject<Long> playerPositionOutput = PublishSubject.create();
  public Observable<Long> playerPositionOutput() {
    return playerPositionOutput;
  }
  private final PublishSubject<KSVideoPlayer> playerIsPrepared = PublishSubject.create();
  public Observable<KSVideoPlayer> playerIsPrepared() {
    return playerIsPrepared;
  }

  // ERRORS

  public final VideoViewModelInputs inputs = this;
  public final VideoViewModelOutputs outputs = this;
  public final VideoViewModelErrors errors = this;

  @Override
  public void playerNeedsPrepare(final @NonNull Video video, final long position, final @NonNull SurfaceView surfaceView,
    final @NonNull View rootView) {
    final List<Object> videoPositionSurfaceRoot = Arrays.asList(video, position, surfaceView, rootView);
    this.playerNeedsPrepare.onNext(videoPositionSurfaceRoot);
  }

  @Override
  public void playerNeedsRelease(final @Nullable KSVideoPlayer player) {
    this.playerNeedsRelease.onNext(player);
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    addSubscription(
      playerNeedsPrepare
        .subscribe(videoSurfaceRoot -> {
          final Video video = (Video) videoSurfaceRoot.get(0);
          final long position = (Long) videoSurfaceRoot.get(1);
          final SurfaceView surfaceView = (SurfaceView) videoSurfaceRoot.get(2);
          final View rootView = (View) videoSurfaceRoot.get(3);
          preparePlayer(context, video, position, surfaceView, rootView);
        })
    );

    addSubscription(
      playerNeedsRelease
        .filter(p -> p != null)
        .subscribe(this::releasePlayer)
    );

    // todo
    addSubscription(videoEnded.subscribe(__ -> koala.trackVideoCompleted()));
  }

  @Override
  public void onStateChanged(final boolean playWhenReady, final int state) {
    playbackState.onNext(state);

    switch (state) {
      case ExoPlayer.STATE_ENDED:
        videoEnded.onNext(null);
        break;
    }
  }

  public void preparePlayer(final @NonNull Context context, final @NonNull Video video, final long position,
    final @NonNull SurfaceView surfaceView, final @NonNull View rootView) {
    final KSVideoPlayer player = new KSVideoPlayer(new KSRendererBuilder(context, video.high()));
    player.setListener(this);
    player.seekTo(position); // todo: will be used for inline video playing

    final MediaController mediaController = new MediaController(context);
    mediaController.setMediaPlayer(player.getPlayerControl());
    mediaController.setAnchorView(rootView);
    mediaController.setEnabled(true);

    player.prepare();
    player.setSurface(surfaceView.getHolder().getSurface());
    player.setPlayWhenReady(true);

    playerIsPrepared.onNext(player);
    mediaControllerBehaviorSubject.onNext(mediaController);

    addSubscription(
      RxView.clicks(rootView)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> toggleController(mediaController))
    );
  }

  public void releasePlayer(final @NonNull KSVideoPlayer player) {
    playerPositionOutput.onNext(player.getCurrentPosition());
    player.release();
  }

  public void toggleController(final @NonNull MediaController mediaController) {
    if (mediaController.isShowing()) {
      mediaController.hide();
    } else {
      mediaController.show(0);
    }
  }
}
