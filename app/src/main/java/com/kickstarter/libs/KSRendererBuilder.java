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
 * Original: https://github.com/google/ExoPlayer/blob/bcb9f8282df7b05a66329e5115511c67f1651d2d/demo/src/main/java/com/google/android/exoplayer/demo/player/ExtractorRendererBuilder.java
 * Modifications: Kickstarter have added some modifiers and annotations. `BandwidthMeter` and `TextTrackRenderer` have
 *   also been removed.
 */

package com.kickstarter.libs;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.kickstarter.libs.KSVideoPlayer.RendererBuilder;

public class KSRendererBuilder implements RendererBuilder {
  public static final int BUFFER_SEGMENT_COUNT = 256;
  public static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
  private Context context;
  private String videoLink;

  public KSRendererBuilder(final @NonNull Context context, final @NonNull String videoLink) {
    this.context = context;
    this.videoLink = videoLink;
  }

  @Override
  public void buildRenderers(final @NonNull KSVideoPlayer player) {
    final Allocator allocator = new DefaultAllocator(true, BUFFER_SEGMENT_SIZE);
    final DataSource dataSource = new DefaultDataSource(this.context, null, this.videoLink, true);
    final ExtractorMediaSource sampleSource = new ExtractorMediaSource.Factory(Uri.parse(this.videoLink), dataSource,
      allocator, BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);

    final MediaCodecVideoRenderer videoRenderer = new MediaCodecVideoRenderer(this.context, sampleSource,
      MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
    final MediaCodecAudioRenderer audioRenderer = new MediaCodecAudioRenderer(sampleSource, MediaCodecSelector.DEFAULT);

    player.prepareRenderers(videoRenderer, audioRenderer);
  }
}
