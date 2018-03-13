/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ***
 *
 * Original: https://github.com/google/ExoPlayer/blob/bcb9f8282df7b05a66329e5115511c67f1651d2d/demo/src/main/java/com/google/android/exoplayer/demo/player/DemoPlayer.java
 * Modifications: Kickstarter have added some modifiers and annotations. References to DRM, metadata and other extraneous
 *   features from the original `DemoPlayer` have been removed.
 */

package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.Surface;

import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;

import static android.os.Build.VERSION_CODES.M;

/**
 * ExoPlayer wrapper that provides higher level interface.
 */
public final class KSVideoPlayer extends SimpleExoPlayer implements Player.EventListener {
  private static final int TRACK_RENDERER_COUNT = 3; // audio, video, text
  private boolean lastReportedPlayWhenReady;
  private int lastReportedPlaybackState;
  private final ExoPlayer player;
  private MediaCodecVideoRenderer videoRenderer;
  private final RendererBuilder rendererBuilder;
  private Surface surface;
  private Listener listener;

  public interface Listener {
    void onStateChanged(boolean playWhenReady, int playbackState);
  }

  public interface RendererBuilder {
    void buildRenderers(KSVideoPlayer player);
  }

  public KSVideoPlayer(final @NonNull RendererBuilder rendererBuilder) {
    super();
    this.player = ExoPlayerFactory.newInstance().Factory.newInstance(TRACK_RENDERER_COUNT);
    this.rendererBuilder = rendererBuilder;
    this.player.addListener(this);
  }

  @Override
  public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

  }
  @Override
  public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

  }
  @Override
  public void onLoadingChanged(boolean isLoading) {

  }
  @Override
  public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
    reportPlayerState();
  }
  @Override
  public void onRepeatModeChanged(int repeatMode) {

  }
  @Override
  public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

  }

  @Override
  public void onPlayerError(final @NonNull ExoPlaybackException error) {}
  @Override
  public void onPositionDiscontinuity(int reason) {

  }
  @Override
  public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

  }
  @Override
  public void onSeekProcessed() {

  }

  /* ExoPlayer helpers */
  public long getCurrentPosition() {
    return this.player.getCurrentPosition();
  }

  public long getDuration() {
    return this.player.getDuration();
  }

  public int getPlaybackState() {
    return this.player.getPlaybackState();
  }

  public void pushSurface(final boolean blockForSurfacePush) {
    if (this.videoRenderer == null) {
      return;
    }
    if (blockForSurfacePush) {
      this.player.createMessage(this.videoRenderer, C.MSG_SET_SURFACE, this.surface);
    } else {
      this.player.sendMessages(this.videoRenderer, C.MSG_SET_SURFACE, this.surface);
    }
  }

  public void prepare() {
    this.videoRenderer = null;
    reportPlayerState();
    this.rendererBuilder.buildRenderers(this);
  }

  public void prepareRenderers(final @NonNull MediaCodecVideoRenderer videoRenderer,
    final @NonNull MediaCodecAudioRenderer audioTrackRenderer) {
    this.videoRenderer = videoRenderer;
    this.player.sendMessages(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, this.surface);
    this.player.prepare(videoRenderer, audioTrackRenderer);
  }

  public void release() {
    this.surface = null;
    this.player.release();
  }

  public void reportPlayerState() {
    final boolean playWhenReady = this.player.getPlayWhenReady();
    final int playbackState = this.player.getPlaybackState();

    if (this.lastReportedPlayWhenReady != playWhenReady || this.lastReportedPlaybackState != playbackState) {
      this.listener.onStateChanged(playWhenReady, playbackState);
    }

    this.lastReportedPlaybackState = playbackState;
    this.lastReportedPlayWhenReady = playWhenReady;
  }

  public void seekTo(final long position) {
    this.player.seekTo(position);
  }

  public void setListener(final @NonNull Listener listener) {
    this.listener = listener;
  }

  public void setPlayWhenReady(final boolean playWhenReady) {
    this.player.setPlayWhenReady(playWhenReady);
  }

  public void setSurface(final @NonNull Surface surface) {
    this.surface = surface;
    pushSurface(false);
  }
}
