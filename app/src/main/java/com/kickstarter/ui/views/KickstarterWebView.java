package com.kickstarter.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.kickstarter.KsrApplication;
import com.kickstarter.services.KickstarterWebViewClient;

import javax.inject.Inject;

public class KickstarterWebView extends WebView {
  @Inject KickstarterWebViewClient client;

  public KickstarterWebView(final Context context) {
    this(context, null);
  }

  public KickstarterWebView(final Context context, final AttributeSet attrs) {
    this(context, attrs, android.R.attr.webViewStyle);
  }

  public KickstarterWebView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);

    ((KsrApplication) context.getApplicationContext()).component().inject(this);
    setWebViewClient(client);
  }
}
