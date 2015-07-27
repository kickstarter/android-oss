package com.kickstarter.libs;

import android.webkit.JavascriptInterface;

import com.kickstarter.services.KickstarterWebViewClient;

public class WebViewJavascriptInterface {
  private final KickstarterWebViewClient kickstarterWebViewClient;

  public WebViewJavascriptInterface(final KickstarterWebViewClient kickstarterWebViewClient) {
    this.kickstarterWebViewClient = kickstarterWebViewClient;
  }

  public class FormContents {
    public final String serialized;
    public final String method;
    public final String encodingType;
    public final String authenticityToken;

    public FormContents(final String serialized, final String method, final String encodingType, final String authenticityToken) {
      this.serialized = serialized;
      this.method = method;
      this.encodingType = encodingType;
      this.authenticityToken = authenticityToken;
    }
  }

  @JavascriptInterface
  public void setFormContents(final String serialized, final String method, final String encodingType, final String authenticityToken) {
    kickstarterWebViewClient.setFormContents(new FormContents(serialized, method, encodingType, authenticityToken));
  }
}

