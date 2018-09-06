package com.kickstarter.mock.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.Photo;

public final class PhotoFactory {
  private PhotoFactory() {}

  public static @NonNull Photo photo() {
    final String url = "https://ksr-ugc.imgix.net/projects/1176555/photo-original.png?w=1536&h=864&fit=fill&bg=FFFFFF&v=1407175667&auto=format&q=92&s=c9e2c12677f912b1921bb6a0912fe910";

    return Photo.builder()
      .ed(url)
      .full(url)
      .little(url)
      .med(url)
      .small(url)
      .thumb(url)
      .build();
  }
}
