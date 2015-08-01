package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CheckoutPresenter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(CheckoutPresenter.class)
public class CheckoutActivity extends BaseActivity<CheckoutPresenter> {
  public @InjectView(R.id.web_view) WebView webView;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.checkout_layout);
    ButterKnife.inject(this);

    final Intent intent = getIntent();
    final String url = intent.getExtras().getString("url");
    presenter.takeProject(intent.getExtras().getParcelable("project"));

    webView.loadUrl(url);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Timber.d("onBackPressed");

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void onSignupUriRequest() {
    Timber.d("onSignupUriRequest");
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra("forward", true);
    startActivityForResult(intent,
      ActivityRequestCodes.CHECKOUT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
  }

  public void onCheckoutThanksUriRequest() {
    Timber.d("onCheckoutThanksUriRequest");
    presenter.takeCheckoutThanksUriRequest();
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
    Timber.d("onActivityResult, requestCode is " + requestCode);
    if (requestCode != ActivityRequestCodes.CHECKOUT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED) {
      return;
    }

    if (resultCode != RESULT_OK) {
      finish();
      return;
    }

    presenter.takeLoginSuccess();
  }
}
