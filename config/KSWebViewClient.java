package com.kickstarter.services;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kickstarter.libs.FormContents;

import java.util.List;

import okhttp3.OkHttpClient;

public final class KSWebViewClient extends WebViewClient {

  public interface Delegate {
    void externalLinkActivated(final @NonNull String url);
    void onPageFinished(final @Nullable String url);
    void onPageStarted(final @Nullable String url);
    void pageIntercepted(final @NonNull String url);
    void onReceivedError(final @NonNull String url);
  }

  public KSWebViewClient(final @NonNull OkHttpClient client, final @NonNull String webEndpoint, final @NonNull PerimeterXClientType manager) {
    this(client, webEndpoint, null, manager);
  }

  public KSWebViewClient(final @NonNull OkHttpClient client,
                         final @NonNull String webEndpoint,
                         final @Nullable Delegate delegate,
                         final @NonNull PerimeterXClientType manager) {
  }

  public void setDelegate(final @Nullable Delegate delegate) {
  }

  public @Nullable Delegate delegate() {
    return null;
  }

  @Override
  public void onPageStarted(final @Nullable WebView view, final @Nullable String url, final @Nullable Bitmap favicon) {
  }

  @Override
  public void onPageFinished(final @NonNull WebView view, final @NonNull String url) {
  }

  @Override
  public boolean shouldOverrideUrlLoading(final @NonNull WebView view, final @NonNull String url) {
    return false;
  }

  @Override
  public WebResourceResponse shouldInterceptRequest(final @NonNull WebView view, final @NonNull String url) {
    return null;
  }

  // The order of request handlers is important - we iterate through the request handlers
  // sequentially until a match is found.
  public void registerRequestHandlers(final @NonNull List<RequestHandler> requestHandlers) {
  }

  public void setFormContents(final @NonNull FormContents formContents) {
  }
}
