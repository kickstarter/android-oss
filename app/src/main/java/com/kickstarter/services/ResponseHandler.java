package com.kickstarter.services;

import android.net.Uri;
import android.webkit.WebView;

import com.squareup.okhttp.Response;

import java.util.regex.Pattern;

public class ResponseHandler {
  private final ResponseHandler.Matcher matcher;
  private final ResponseHandler.Action action;

  public ResponseHandler(final ResponseHandler.Matcher matcher, final ResponseHandler.Action action) {
    this.matcher = matcher;
    this.action = action;
  }

  public boolean matches(final Uri uri, final String webEndpoint) {
    return matcher.call(uri, webEndpoint);
  }

  public boolean action(final Response response, final WebView webView) {
    return action.call(response, webView);
  }

  public interface Matcher {
    boolean call(final Uri uri, final String webEndpoint);
  }

  public interface Action {
    boolean call(final Response response, final WebView webView);
  }
}
