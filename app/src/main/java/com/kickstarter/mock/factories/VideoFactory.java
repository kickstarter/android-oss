package com.kickstarter.mock.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.Video;

public final class VideoFactory {
  private VideoFactory() {}

  public static @NonNull Video video() {
    return Video.builder()
      .base("https://www.kickstarter.com/project/base.mp4")
      .frame("https://www.kickstarter.com/project/frame.mp4")
      .high("https://www.kickstarter.com/project/high.mp4")
      .build();
  }

  public static @NonNull Video hlsVideo() {
    return Video.builder()
      .base("https://www.kickstarter.com/project/base.mp4")
      .frame("https://www.kickstarter.com/project/frame.mp4")
      .high("https://www.kickstarter.com/project/high.mp4")
      .hls("https://ksr-video.imgix.net/projects/3275127/video-865539-hls_playlist.m3u8")
      .build();
  }
}
