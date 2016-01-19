package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.views.KSWebView;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class DisplayWebViewActivity extends BaseActivity {
  protected @Bind(R.id.generic_web_view) KSWebView webView;
  protected @Bind(R.id.web_view_title_text_view) TextView webViewTitleTextView;

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_view_layout);
    ButterKnife.bind(this);

    final String toolbarTitle = getIntent().getExtras().getString(IntentKey.TOOLBAR_TITLE, "");
    webViewTitleTextView.setText(toolbarTitle);

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
