package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;

import com.google.android.exoplayer.ExoPlayer;
import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.KSApplication;
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
  private final BehaviorSubject<Long> currentPosition = BehaviorSubject.create(0l);
  private final BehaviorSubject<MediaController> mediaControllerBehaviorSubject = BehaviorSubject.create();
  private final PublishSubject<KSVideoPlayer> playerIsPrepared = PublishSubject.create();
  private final PublishSubject<List<Object>> playerNeedsPrepare = PublishSubject.create();
  private final PublishSubject<KSVideoPlayer> playerNeedsRelease = PublishSubject.create();
  private final PublishSubject<Void> videoEnded = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Integer> playbackState = PublishSubject.create();
  public Observable<Integer> playbackState() {
    return playbackState;
  }

  // ERRORS

  public final VideoViewModelInputs inputs = this;
  public final VideoViewModelOutputs outputs = this;
  public final VideoViewModelErrors errors = this;

  @Override
  public void playerNeedsPrepare(final @NonNull Video video, final @NonNull SurfaceView surfaceView,
    final @NonNull View rootView) {
    final List<Object> videoPositionSurfaceRoot = Arrays.asList(video, surfaceView, rootView);
    this.playerNeedsPrepare.onNext(videoPositionSurfaceRoot);
  }

  @Override
  public void playerNeedsRelease() {
    this.playerNeedsRelease.onNext(null);
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    addSubscription(
      playerIsPrepared
        .compose(Transformers.takeWhen(playerNeedsRelease))
        .filter(p -> p != null)
        .subscribe(this::releasePlayer)
    );

    addSubscription(
      playerNeedsPrepare
        .subscribe(videoSurfaceRootPosition -> {
          final Video video = (Video) videoSurfaceRootPosition.get(0);
          final SurfaceView surfaceView = (SurfaceView) videoSurfaceRootPosition.get(1);
          final View rootView = (View) videoSurfaceRootPosition.get(2);
          preparePlayer(context, video, currentPosition.getValue(), surfaceView, rootView);
        })
    );

    addSubscription(
      mediaControllerBehaviorSubject
        .compose(Transformers.takeWhen(videoEnded))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(MediaController::show)
    );
  }

  @Override
  public void onStateChanged(final boolean playWhenReady, final int state) {
    playbackState.onNext(state);
    if (playWhenReady) {
      koala.trackVideoResume();
    } else {
      koala.trackVideoPaused();
    }

    switch (state) {
      case ExoPlayer.STATE_ENDED:
        videoEnded.onNext(null);
        koala.trackVideoCompleted();
        break;
    }
  }

  public void preparePlayer(final @NonNull Context context, final @NonNull Video video, final long position,
    final @NonNull SurfaceView surfaceView, final @NonNull View rootView) {
    final KSVideoPlayer player = new KSVideoPlayer(context, new KSRendererBuilder(context, video.high()));
    player.setListener(this);
    player.seekTo(position);

    final MediaController mediaController = new MediaController(context);
    mediaController.setMediaPlayer(player.getPlayerControl());
    mediaController.setAnchorView(rootView);
    mediaController.setEnabled(true);

    player.prepare();
    player.setSurface(surfaceView.getHolder().getSurface());
    player.setPlayWhenReady(true);

    addSubscription(
      RxView.clicks(rootView)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> toggleController(mediaController))
    );

    playerIsPrepared.onNext(player);
    mediaControllerBehaviorSubject.onNext(mediaController);
  }

  public void releasePlayer(final @NonNull KSVideoPlayer ksVideoPlayer) {
    currentPosition.onNext(ksVideoPlayer.getCurrentPosition());
    ksVideoPlayer.release();
  }

  public void toggleController(final @NonNull MediaController mediaController) {
    if (mediaController.isShowing()) {
      mediaController.hide();
    } else {
      mediaController.show();
    }
  }
}
