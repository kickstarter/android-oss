package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import okhttp3.OkHttpClient;

public final class SocketUtils {
  private SocketUtils() {}

  public static @NonNull OkHttpClient enableTLS1_2OnPreLollipop(final @NonNull OkHttpClient client) {
    return client;
  }
}
