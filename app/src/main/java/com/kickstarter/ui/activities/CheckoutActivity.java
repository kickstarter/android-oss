package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.ui.toolbars.KSToolbar;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.CheckoutViewModel;
import com.squareup.okhttp.Request;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(CheckoutViewModel.class)
public final class CheckoutActivity extends BaseActivity<CheckoutViewModel> implements KSWebViewClient.Delegate {
  private Project project;
  @Bind(R.id.checkout_toolbar) KSToolbar checkoutToolbar;
  @Bind(R.id.web_view) KSWebView webView;
  @Bind(R.id.checkout_loading_indicator) View loadingIndicatorView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.checkout_layout);
    ButterKnife.bind(this);

    webView.client().registerRequestHandlers(Arrays.asList(
      new RequestHandler(KSUri::isCheckoutThanksUri, this::handleCheckoutThanksUriRequest),
      new RequestHandler(KSUri::isSignupUri, this::handleSignupUriRequest)
    ));

    webView.client().setDelegate(this);

    viewModel.outputs.project()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(project -> this.project = project);

    viewModel.outputs.title()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(checkoutToolbar::setTitle);
  }

  @Override
  protected void onResume() {
    super.onResume();

    viewModel.outputs.url()
      .take(1)
      .subscribe(webView::loadUrl);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  private boolean handleCheckoutThanksUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    final Intent intent = new Intent(this, ThanksActivity.class)
      .putExtra(IntentKey.PROJECT, project);

    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);

    return true;
  }

  private boolean handleSignupUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(IntentKey.LOGIN_REASON, LoginReason.BACK_PROJECT);
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
    return true;
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final @Nullable Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);

    if (requestCode != ActivityRequestCodes.LOGIN_FLOW) {
      return;
    }

    if (resultCode != RESULT_OK) {
      finish();
    }
  }

  @Override
  public void webViewOnPageStarted(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {
    final AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
    animation.setDuration(300l);
    animation.setFillAfter(true);
    loadingIndicatorView.startAnimation(animation);
  }

  @Override
  public void webViewOnPageFinished(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {
    final AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
    animation.setDuration(300l);
    animation.setFillAfter(true);
    loadingIndicatorView.startAnimation(animation);
  }

  @Override
  public void webViewPageIntercepted(final @NonNull KSWebViewClient webViewClient, final @NonNull String url) {
    viewModel.inputs.pageIntercepted(url);
  }
}
