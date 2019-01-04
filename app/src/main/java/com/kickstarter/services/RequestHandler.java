package com.kickstarter.services;

import android.net.Uri;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import okhttp3.Request;

public final class RequestHandler {
  private final RequestHandler.Matcher matcher;
  private final RequestHandler.Action action;

  public RequestHandler(final @NonNull RequestHandler.Matcher matcher, final @NonNull RequestHandler.Action action) {
    this.matcher = matcher;
    this.action = action;
  }

  public boolean matches(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return this.matcher.call(uri, webEndpoint);
  }

  public boolean action(final @NonNull Request request, final @NonNull WebView webView) {
    return this.action.call(request, webView);
  }

  public interface Matcher {
    boolean call(final @NonNull Uri uri, final @NonNull String webEndpoint);
  }

  public interface Action {
    boolean call(final @NonNull Request request, final @NonNull WebView webView);
  }
}
