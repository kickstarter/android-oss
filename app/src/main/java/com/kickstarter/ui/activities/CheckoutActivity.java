package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CheckoutPresenter;
import com.kickstarter.ui.views.KickstarterWebView;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

@RequiresPresenter(CheckoutPresenter.class)
public class CheckoutActivity extends BaseActivity<CheckoutPresenter> {
  public @Bind(R.id.web_view) KickstarterWebView webView;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.checkout_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    final String url = intent.getExtras().getString(getString(R.string.intent_url));
    final Project project = intent.getExtras().getParcelable(getString(R.string.intent_project));
    presenter.initialize(project, url);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void startLoginToutActivity() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(getString(R.string.intent_forward), true);
    startActivityForResult(intent,
      ActivityRequestCodes.CHECKOUT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
  }

  public void startThanksActivity(@NonNull final Project project) {
    final Intent intent = new Intent(this, ThanksActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent intent) {
    if (requestCode != ActivityRequestCodes.CHECKOUT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED) {
      return;
    }

    if (resultCode != RESULT_OK) {
      finish();
      return;
    }

    Timber.d("onActivityResult", this.toString());

    presenter.takeLoginSuccess();
  }
}
