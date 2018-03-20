package com.kickstarter.libs.utils;

import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Util;

public class MediaSourceUtil {

  public static MediaSource getMediaSourceForUrl(DataSource.Factory dataSourceFactory, String videoUrl) {
    Uri videoUri = Uri.parse(videoUrl);
    int fileType = Util.inferContentType(videoUri);

    switch (fileType) {
      case C.TYPE_HLS:
        return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri);
      default: {
        return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(videoUri);
      }
    }
  }

}
