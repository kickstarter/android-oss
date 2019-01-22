package com.kickstarter.libs;

import androidx.annotation.NonNull;
import android.webkit.JavascriptInterface;

import com.kickstarter.services.KSWebViewClient;

public class WebViewJavascriptInterface {
  private final KSWebViewClient webViewClient;

  public WebViewJavascriptInterface(final @NonNull KSWebViewClient webViewClient) {
    this.webViewClient = webViewClient;
  }

  @JavascriptInterface
  public void setFormContents(final @NonNull String serialized, final @NonNull String method,
    final @NonNull String encodingType) {
  }
}
