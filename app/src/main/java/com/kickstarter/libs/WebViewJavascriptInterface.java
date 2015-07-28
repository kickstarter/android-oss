package com.kickstarter.libs;

import android.webkit.JavascriptInterface;

import com.kickstarter.services.KickstarterWebViewClient;

public class WebViewJavascriptInterface {
  private final KickstarterWebViewClient kickstarterWebViewClient;

  public WebViewJavascriptInterface(final KickstarterWebViewClient kickstarterWebViewClient) {
    this.kickstarterWebViewClient = kickstarterWebViewClient;
  }

  @JavascriptInterface
  public void setFormContents(final String serialized, final String method, final String encodingType, final String authenticityToken) {
    kickstarterWebViewClient.setFormContents(new FormContents(serialized, method, encodingType, authenticityToken));
  }
}

