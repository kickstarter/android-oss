package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.ui.views.KickstarterWebView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DisplayWebViewActivity extends BaseActivity {
  @InjectView(R.id.generic_webview) KickstarterWebView webView;

  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.webview_layout);
    ButterKnife.inject(this);

    final Intent intent = getIntent();
    final String url = intent.getExtras().getString("url");

    webView.loadUrl(url);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }
}
