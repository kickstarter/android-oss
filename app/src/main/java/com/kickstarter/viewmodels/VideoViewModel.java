package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceView;
import android.widget.ProgressBar;

import com.kickstarter.libs.KSRendererBuilder;
import com.kickstarter.libs.KSVideoPlayer;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.models.Video;
import com.kickstarter.ui.activities.VideoActivity;
import com.kickstarter.viewmodels.errors.VideoViewModelErrors;
import com.kickstarter.viewmodels.inputs.VideoViewModelInputs;
import com.kickstarter.viewmodels.outputs.VideoViewModelOutputs;

import rx.Observable;
import rx.subjects.PublishSubject;

public class VideoViewModel extends ViewModel<VideoActivity> implements VideoViewModelInputs, VideoViewModelOutputs,
  VideoViewModelErrors, KSVideoPlayer.Listener {

  // INPUTS
  private final PublishSubject<ProgressBar> loadingIndicator = PublishSubject.create();
  private final PublishSubject<Pair<Video, SurfaceView>> playerNeedsPrepare = PublishSubject.create();
  private final PublishSubject<KSVideoPlayer> playerNeedsRelease = PublishSubject.create();

  // OUTPUTS
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
  public void loadingIndicator(final @NonNull ProgressBar progressBar) {
    this.loadingIndicator.onNext(progressBar);
  }

  @Override
  public void playerNeedsPrepare(final @NonNull Video video, final @NonNull SurfaceView surfaceView) {
    this.playerNeedsPrepare.onNext(new Pair<>(video, surfaceView));
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
        .subscribe(videoAndSurface -> {
          preparePlayer(context, videoAndSurface.first, videoAndSurface.second);
        })
    );

    addSubscription(
      playerNeedsRelease
        .filter(p -> p != null)
        .subscribe(this::releasePlayer)
    );

  }

  @Override
  public void onStateChanged(final boolean playWhenReady, final int playbackState) {
    Log.d("TEST", "state: " + playbackState);

//    if (playbackState == ExoPlayer.STATE_ENDED) {
//      mediaController.show();
//    }
//
//    if (playbackState == ExoPlayer.STATE_BUFFERING) {
//      loadingIndicatorProgressBar.setVisibility(View.VISIBLE);
//    } else {
//      loadingIndicatorProgressBar.setVisibility(View.GONE);
//    }
  }

  public void preparePlayer(final @NonNull Context context, final @NonNull Video video,
    final @NonNull SurfaceView surfaceView) {
    final KSVideoPlayer player = new KSVideoPlayer(new KSRendererBuilder(context, video.high()));
    player.setListener(this);
//    player.seekTo(position); // todo: will be used for inline video playing

    player.prepare();
    player.setSurface(surfaceView.getHolder().getSurface());
    player.setPlayWhenReady(true);

    playerIsPrepared.onNext(player);
  }

  public void releasePlayer(final @NonNull KSVideoPlayer player) {
    playerPositionOutput.onNext(player.getCurrentPosition());
    player.release();
  }
}
