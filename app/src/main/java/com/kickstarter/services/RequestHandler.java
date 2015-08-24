package com.kickstarter.services;

import android.net.Uri;
import android.webkit.WebView;

import com.squareup.okhttp.Request;

public class RequestHandler {
  private final RequestHandler.Matcher matcher;
  private final RequestHandler.Action action;

  public RequestHandler(final RequestHandler.Matcher matcher, final RequestHandler.Action action) {
    this.matcher = matcher;
    this.action = action;
  }

  public boolean matches(final Uri uri, final String webEndpoint) {
    return matcher.call(uri, webEndpoint);
  }

  public boolean action(final Request request, final WebView webView) {
    return action.call(request, webView);
  }

  public interface Matcher {
    boolean call(final Uri uri, final String webEndpoint);
  }

  public interface Action {
    boolean call(final Request request, final WebView webView);
  }
}
