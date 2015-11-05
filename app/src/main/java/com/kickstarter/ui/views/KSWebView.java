package com.kickstarter.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.WebViewJavascriptInterface;
import com.kickstarter.services.KSWebViewClient;

import javax.inject.Inject;

public class KSWebView extends WebView {
  @Inject KSWebViewClient client;

  public KSWebView(@NonNull final Context context) {
    this(context, null);
  }

  public KSWebView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    this(context, attrs, android.R.attr.webViewStyle);
  }

  public KSWebView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);

    if (isInEditMode()) {
      return;
    }

    ((KSApplication) context.getApplicationContext()).component().inject(this);
    setWebViewClient(client);
    setWebChromeClient(new WebChromeClient());
    getSettings().setJavaScriptEnabled(true);
    getSettings().setAllowFileAccess(false);

    if (ApiCapabilities.canDebugWebViews()) {
      setWebContentsDebuggingEnabled(true);
    }

    addJavascriptInterface(new WebViewJavascriptInterface(this.client), "WebViewJavascriptInterface");
  }

  public KSWebViewClient client() {
    return client;
  }

  /**
   * Returns last Url String handled by the web view client.
   */
  public String lastClientUrl() {
    return client.lastKickstarterUrl();
  }
}
