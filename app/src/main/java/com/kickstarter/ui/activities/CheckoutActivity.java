package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.WebView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CheckoutPresenter;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.views.KSWebView;
import com.squareup.okhttp.Request;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

@RequiresPresenter(CheckoutPresenter.class)
public final class CheckoutActivity extends BaseActivity<CheckoutPresenter> {
  private Project project;
  private String urlToReload;
  @Bind(R.id.web_view) KSWebView webView;
  public @Bind(R.id.toolbar_title) TextView toolbarTitleTextView;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.checkout_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    if (savedInstanceState == null) {
      urlToReload = intent.getExtras().getString(getString(R.string.intent_url));
    }
    project = intent.getExtras().getParcelable(getString(R.string.intent_project));
    toolbarTitleTextView.setText(intent.getStringExtra(getString(R.string.intent_toolbar_title)));

    webView.client().registerRequestHandlers(Arrays.asList(
      new RequestHandler(KSUri::isCheckoutThanksUri, this::handleCheckoutThanksUriRequest),
      new RequestHandler(KSUri::isSignupUri, this::handleSignupUriRequest)
    ));
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
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
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
