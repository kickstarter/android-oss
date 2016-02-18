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

import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
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
    final Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
    final DataSource dataSource = new DefaultUriDataSource(context, videoLink);
    final ExtractorSampleSource sampleSource = new ExtractorSampleSource(Uri.parse(videoLink), dataSource,
      allocator, BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);

    final MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context, sampleSource,
      MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
    final MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);

    player.prepareRenderers(videoRenderer, audioRenderer);
  }
}
