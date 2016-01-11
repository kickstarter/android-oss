package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.views.KSWebView;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class DisplayWebViewActivity extends BaseActivity {
  protected @Bind(R.id.generic_webview) KSWebView webView;

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_view_layout);
    ButterKnife.bind(this);

    final String url = getIntent().getExtras().getString(IntentKey.URL);

    webView.loadUrl(url);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overrideExitTransition();
  }

  private void overrideExitTransition() {
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }
}
