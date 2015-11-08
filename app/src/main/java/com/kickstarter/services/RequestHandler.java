package com.kickstarter.services;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.WebView;

import com.squareup.okhttp.Request;

public final class RequestHandler {
  private final RequestHandler.Matcher matcher;
  private final RequestHandler.Action action;

  public RequestHandler(@NonNull final RequestHandler.Matcher matcher, @NonNull final RequestHandler.Action action) {
    this.matcher = matcher;
    this.action = action;
  }

  public boolean matches(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return matcher.call(uri, webEndpoint);
  }

  public boolean action(@NonNull final Request request, @NonNull final WebView webView) {
    return action.call(request, webView);
  }

  public interface Matcher {
    boolean call(@NonNull final Uri uri, @NonNull final String webEndpoint);
  }

  public interface Action {
    boolean call(@NonNull final Request request, @NonNull final WebView webView);
  }
}
