package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.view.Surface;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.util.PlayerControl;

/**
 * ExoPlayer wrapper that provides higher level interface.
 * Reference: https://github.com/google/ExoPlayer/blob/master/demo/src/main/java/com/google/android/exoplayer/demo/player/DemoPlayer.java
 */
public final class KSVideoPlayer implements ExoPlayer.Listener {
  private final int TRACK_RENDERER_COUNT = 3; // audio, video, text
  private boolean lastReportedPlayWhenReady;
  private int lastReportedPlaybackState;
  private final ExoPlayer player;
  private MediaCodecVideoTrackRenderer videoRenderer;
  private PlayerControl playerControl;
  private final RendererBuilder rendererBuilder;
  private Surface surface;
  private Listener listener;

  public interface Listener {
    void onStateChanged(boolean playWhenReady, int playbackState);
  }

  public interface RendererBuilder {
    void buildRenderers(KSVideoPlayer player);
  }

  public KSVideoPlayer(@NonNull final RendererBuilder rendererBuilder) {
    this.player = ExoPlayer.Factory.newInstance(TRACK_RENDERER_COUNT);
    this.rendererBuilder = rendererBuilder;
    playerControl = new PlayerControl(player);
    player.addListener(this);
  }

  @Override
  public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
    reportPlayerState();
  }

  @Override
  public void onPlayWhenReadyCommitted() {

  }

  @Override
  public void onPlayerError(@NonNull final ExoPlaybackException error) {

  }

  /* ExoPlayer helpers */
  public long getCurrentPosition() {
    return player.getCurrentPosition();
  }

  public long getDuration() {
    return player.getDuration();
  }

  public PlayerControl getPlayerControl() {
    return playerControl;
  }

  public void pushSurface(final boolean blockForSurfacePush) {
    if (videoRenderer == null) {
      return;
    }
    if (blockForSurfacePush) {
      player.blockingSendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
    } else {
      player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
    }
  }

  public void prepare() {
    videoRenderer = null;
    reportPlayerState();
    rendererBuilder.buildRenderers(this);
  }

  public void prepareRenderers(@NonNull final MediaCodecVideoTrackRenderer videoRenderer,
    @NonNull final MediaCodecAudioTrackRenderer audioTrackRenderer) {
    this.videoRenderer = videoRenderer;
    player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
    player.prepare(videoRenderer, audioTrackRenderer);
  }

  public void release() {
    surface = null;
    player.release();
  }

  public void reportPlayerState() {
    final boolean playWhenReady = player.getPlayWhenReady();
    final int playbackState = player.getPlaybackState();

    if (lastReportedPlayWhenReady != playWhenReady || lastReportedPlaybackState != playbackState) {
      listener.onStateChanged(playWhenReady, playbackState);
    }

    lastReportedPlaybackState = playbackState;
    lastReportedPlayWhenReady = playWhenReady;
  }

  public void seekTo(final long position) {
    player.seekTo(position);
  }

  public void setListener(@NonNull final Listener listener) {
    this.listener = listener;
  }

  public void setPlayWhenReady(final boolean playWhenReady) {
    player.setPlayWhenReady(playWhenReady);
  }

  public void setSurface(@NonNull final Surface surface) {
    this.surface = surface;
    pushSurface(false);
  }
}
