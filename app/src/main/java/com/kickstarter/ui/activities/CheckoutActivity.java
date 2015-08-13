package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CheckoutPresenter;

import butterknife.ButterKnife;
import butterknife.InjectView;

@RequiresPresenter(CheckoutPresenter.class)
public class CheckoutActivity extends BaseActivity<CheckoutPresenter> {
  public @InjectView(R.id.web_view) WebView webView;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.checkout_layout);
    ButterKnife.inject(this);

    final Intent intent = getIntent();
    final String url = intent.getExtras().getString(getString(R.string.intent_url));
    presenter.takeProject(intent.getExtras().getParcelable(getString(R.string.intent_project)));

    webView.loadUrl(url);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void onSignupUriRequest() {
    presenter.takeSignupUriRequest();
  }

  public void startLoginToutActivity() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(getString(R.string.intent_forward), true);
    startActivityForResult(intent,
      ActivityRequestCodes.CHECKOUT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
  }

  public void onCheckoutThanksUriRequest() {
    presenter.takeCheckoutThanksUriRequest();
  }

  public void startThanksActivity(final Project project) {
    final Intent intent = new Intent(this, ThanksActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
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
