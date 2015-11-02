package com.kickstarter.ui.activities;

import android.content.Intent;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.PlayerControl;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.models.Project;
import com.kickstarter.models.Video;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VideoPlayerActivity extends BaseActivity implements ExoPlayer.Listener, SurfaceHolder.Callback {
  private final int TRACK_RENDERER_COUNT = 3; // audio, video, text
  private final int BUFFER_SEGMENT_SIZE = 64 * 1024;
  private final int BUFFER_SEGMENT_COUNT = 256;

  private MediaController mediaController;
  private MediaCodecVideoTrackRenderer videoRenderer;
  private MediaCodecAudioTrackRenderer audioRenderer;
  private ExoPlayer player;
  private boolean playerNeedsPrepare;
  private long playerPosition;
  private Video video;

  public @Bind(R.id.root) View root;
  public @Bind(R.id.video_frame) AspectRatioFrameLayout videoFrame;
  public @Bind(R.id.surface_view) SurfaceView surfaceView;

  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.video_player_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    final Project project = intent.getParcelableExtra(getString(R.string.intent_project));
    video = project.video();

    root.setOnTouchListener(((view, motionEvent) -> {
      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
        toggleControlsVisibility();
      } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
        view.performClick();
      }
      return true;
    }));

    surfaceView.getHolder().addCallback(this);
    mediaController = new MediaController(this);
    mediaController.setAnchorView(root);
  }

  private void preparePlayer(final boolean playWhenReady) {
    if (player == null) {
      player = ExoPlayer.Factory.newInstance(TRACK_RENDERER_COUNT, 1000, 5000);
      player.addListener(this);
      player.seekTo(playerPosition);
      playerNeedsPrepare = true;
      mediaController.setMediaPlayer(new PlayerControl(player));
    }

    if (playerNeedsPrepare) {
      buildRenderers(player, video);
      playerNeedsPrepare = false;
      // update control visibilities
    }

    player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surfaceView.getHolder().getSurface());
    player.setPlayWhenReady(playWhenReady);
  }

  public void buildRenderers(@NonNull final ExoPlayer player, @NonNull final Video video) {
    final Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
    final DataSource dataSource = new DefaultUriDataSource(this, video.high());
    final ExtractorSampleSource sampleSource = new ExtractorSampleSource(Uri.parse(video.high()), dataSource,
      allocator, BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);

    videoRenderer = new MediaCodecVideoTrackRenderer(this, sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
    audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);

    player.prepare(videoRenderer, audioRenderer);
  }

  @Override
  public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {

  }

  @Override
  public void onPlayWhenReadyCommitted() {

  }

  @Override
  public void onPlayerError(@Nullable final ExoPlaybackException error) {

  }

  @Override
  public void onResume() {
    super.onResume();
    if (player == null) {
      preparePlayer(true);
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {

  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

  }

  public void toggleControlsVisibility() {
    if (mediaController.isShowing()) {
      mediaController.hide();
    } else {
      mediaController.show(0);
    }
  }
}
