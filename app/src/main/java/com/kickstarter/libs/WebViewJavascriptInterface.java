package com.kickstarter.libs;

import android.webkit.JavascriptInterface;

import com.kickstarter.services.KickstarterWebViewClient;

public class WebViewJavascriptInterface {
  private final KickstarterWebViewClient kickstarterWebViewClient;

  public WebViewJavascriptInterface(final KickstarterWebViewClient kickstarterWebViewClient) {
    this.kickstarterWebViewClient = kickstarterWebViewClient;
  }

  @JavascriptInterface
  public void setFormContents(final String serialized, final String method, final String encodingType) {
    /*
     *  WebViewJavascript.html is inserted into Kickstarter web view pages - it intercepts form submits,
     *  captures form info (serialized fields, method, encoding type) and sends it to this method, before continuing
     *  on with the form submit. The form info is stored in the web view client.
     *
     *  When the web view client intercepts the outgoing request, it uses the form contents passed to it here to
     *  construct a new request with the correct form body, method and encoding type.
     */
    kickstarterWebViewClient.setFormContents(new FormContents(serialized, method, encodingType));
  }
}

