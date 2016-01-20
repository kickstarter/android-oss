package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.toolbars.KSToolbar;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.WebViewViewModel;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresViewModel(WebViewViewModel.class)
public final class WebViewActivity extends BaseActivity<WebViewViewModel> {
  protected @Bind(R.id.web_view_toolbar) KSToolbar toolbar;
  protected @Bind(R.id.web_view) KSWebView webView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_view_layout);
    ButterKnife.bind(this);

    final String toolbarTitle = getIntent().getExtras().getString(IntentKey.TOOLBAR_TITLE, "");
    toolbar.setTitle(toolbarTitle);

    final String url = getIntent().getExtras().getString(IntentKey.URL);
    webView.loadUrl(url);

    viewModel.inputs.takePushNotificationEnvelope(getIntent().getParcelableExtra(IntentKey.PUSH_NOTIFICATION_ENVELOPE));
  }

  @Override
  @OnClick(R.id.back_button)
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }
}
