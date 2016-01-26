package com.kickstarter.libs;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
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
      MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
    final MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);

    player.prepareRenderers(videoRenderer, audioRenderer);
  }
}
