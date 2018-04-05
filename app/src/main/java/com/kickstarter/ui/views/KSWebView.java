package com.kickstarter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.WebViewJavascriptInterface;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.services.interceptors.WebRequestInterceptor;

import javax.inject.Inject;

public class KSWebView extends WebView {
  @Inject KSWebViewClient client;
  @Inject Build build;

  public KSWebView(final @NonNull Context context) {
    this(context, null);
  }

  public KSWebView(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    this(context, attrs, android.R.attr.webViewStyle);
  }

  @SuppressWarnings("SetJavaScriptEnabled")
  public KSWebView(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);

    if (isInEditMode()) {
      return;
    }

    ((KSApplication) context.getApplicationContext()).component().inject(this);
    setWebViewClient(this.client);
    setWebChromeClient(new WebChromeClient());
    getSettings().setJavaScriptEnabled(true);
    getSettings().setAllowFileAccess(false);
    getSettings().setUserAgentString(WebRequestInterceptor.userAgent(this.build));
    enableDebugging();

    addJavascriptInterface(new WebViewJavascriptInterface(this.client), "WebViewJavascriptInterface");
  }

  public KSWebViewClient client() {
    return this.client;
  }

  @TargetApi(19)
  private void enableDebugging() {
    if (ApiCapabilities.canDebugWebViews()) {
      setWebContentsDebuggingEnabled(true);
    }
  }
}
