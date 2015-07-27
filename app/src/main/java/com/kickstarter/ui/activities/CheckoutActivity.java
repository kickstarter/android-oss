package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CheckoutPresenter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(CheckoutPresenter.class)
public class CheckoutActivity extends BaseActivity<CheckoutPresenter> {
  @InjectView(R.id.web_view) WebView webView;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.checkout_layout);
    ButterKnife.inject(this);

    final Intent intent = getIntent();
    final Project project = intent.getExtras().getParcelable("project");
    final String url = intent.getExtras().getString("url");

    webView.loadUrl(url);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Timber.d("onBackPressed");

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }
}
