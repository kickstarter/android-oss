package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CheckoutPresenter;
import com.kickstarter.services.KickstarterUri;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.views.KickstarterWebView;
import com.squareup.okhttp.Request;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

@RequiresPresenter(CheckoutPresenter.class)
public class CheckoutActivity extends BaseActivity<CheckoutPresenter> {
  private Project project;
  private String urlToReload;
  @Bind(R.id.web_view) KickstarterWebView webView;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.checkout_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    if (savedInstanceState == null) {
      urlToReload = intent.getExtras().getString(getString(R.string.intent_url));
    }

    webView.client().registerRequestHandlers(Arrays.asList(
      new RequestHandler(KickstarterUri::isCheckoutThanksUri, this::handleCheckoutThanksUriRequest),
      new RequestHandler(KickstarterUri::isSignupUri, this::handleSignupUriRequest)
    ));
    project = intent.getExtras().getParcelable(getString(R.string.intent_project));
  }

  @Override
  protected void onRestoreInstanceState(@Nullable final Bundle savedInstanceState) {
   super.onRestoreInstanceState(savedInstanceState);

    if (savedInstanceState != null) {
      urlToReload = savedInstanceState.getString(getString(R.string.save_url));
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (urlToReload != null) {
      webView.loadUrl(urlToReload);
    }
    urlToReload = null;
  }

  @Override
  protected void onSaveInstanceState(@NonNull final Bundle outState) {
    urlToReload = webView.lastClientUrl();
    outState.putString(getString(R.string.save_url), urlToReload);
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  private boolean handleCheckoutThanksUriRequest(@NonNull final Request request, @NonNull final WebView webView) {
    final Intent intent = new Intent(this, ThanksActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
    return true;
  }

  private boolean handleSignupUriRequest(@NonNull final Request request, @NonNull final WebView webView) {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(getString(R.string.intent_forward), true);
    startActivityForResult(intent,
      ActivityRequestCodes.CHECKOUT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
    return true;
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent intent) {
    if (requestCode != ActivityRequestCodes.CHECKOUT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED) {
      return;
    }

    if (resultCode != RESULT_OK) {
      finish();
    }
  }
}
