package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.toolbars.KSToolbar;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.CheckoutViewModel;
import com.squareup.okhttp.Request;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

@RequiresViewModel(CheckoutViewModel.class)
public final class CheckoutActivity extends BaseActivity<CheckoutViewModel> {
  private Project project;
  private String urlToReload;
  @Bind(R.id.checkout_toolbar) KSToolbar checkoutToolbar;
  @Bind(R.id.web_view) KSWebView webView;

  private static String SAVE_URL_KEY = "save_url";

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.checkout_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    if (savedInstanceState == null) {
      urlToReload = intent.getExtras().getString(IntentKey.URL);
    }
    project = intent.getExtras().getParcelable(IntentKey.PROJECT);

    final String title = intent.getExtras().getString(IntentKey.TOOLBAR_TITLE, "");
    checkoutToolbar.setTitle(title);

    webView.client().registerRequestHandlers(Arrays.asList(
      new RequestHandler(KSUri::isCheckoutThanksUri, this::handleCheckoutThanksUriRequest),
      new RequestHandler(KSUri::isSignupUri, this::handleSignupUriRequest)
    ));
  }

  @Override
  protected void onRestoreInstanceState(@Nullable final Bundle savedInstanceState) {
   super.onRestoreInstanceState(savedInstanceState);

    if (savedInstanceState != null) {
      urlToReload = savedInstanceState.getString(SAVE_URL_KEY);
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
    outState.putString(SAVE_URL_KEY, urlToReload);
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  private boolean handleCheckoutThanksUriRequest(@NonNull final Request request, @NonNull final WebView webView) {
    final Intent intent = new Intent(this, ThanksActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
    return true;
  }

  private boolean handleSignupUriRequest(@NonNull final Request request, @NonNull final WebView webView) {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(IntentKey.FORWARD, true)
      .putExtra(IntentKey.LOGIN_TYPE, LoginToutActivity.REASON_BACK_PROJECT);
    startActivityForResult(intent, ActivityRequestCodes.CHECKOUT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
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
