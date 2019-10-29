package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.ui.toolbars.KSToolbar;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.CheckoutViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import okhttp3.Request;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(CheckoutViewModel.ViewModel.class)
public final class CheckoutActivity extends BaseActivity<CheckoutViewModel.ViewModel> implements KSWebView.Delegate {
  private @Nullable Project project;

  protected @Bind(R.id.checkout_toolbar) KSToolbar checkoutToolbar;
  protected @Bind(R.id.web_view) KSWebView webView;

  protected @BindString(R.string.profile_settings_about_terms) String termsOfUseString;
  protected @BindString(R.string.profile_settings_about_privacy) String privacyPolicyString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.checkout_layout);
    ButterKnife.bind(this);

    this.webView.setDelegate(this);
    this.webView.registerRequestHandlers(Arrays.asList(
      new RequestHandler(KSUri::isCheckoutThanksUri, this::handleCheckoutThanksUriRequest),
      new RequestHandler(KSUri::isNewGuestCheckoutUri, this::handleSignupUriRequest),
      new RequestHandler(KSUri::isSignupUri, this::handleSignupUriRequest)
    ));

    this.viewModel.outputs.project()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(p -> this.project = p);

    this.viewModel.outputs.title()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.checkoutToolbar::setTitle);

    this.viewModel.outputs.popActivityOffStack()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.popActivity());
  }

  @Override
  protected void onResume() {
    super.onResume();

    this.viewModel.outputs.url()
      .take(1)
      .subscribe(this.webView::loadUrl);
  }

  private void popActivity() {
    super.back();
  }

  /**
   * This method is called from {@link com.kickstarter.services.KSWebViewClient} when an Android Pay
   * payload has been obtained from the webview.
   *
   * @deprecated 10/25/2019: We no longer support Android Pay from the webview.
   */
  @Deprecated
  public void takeAndroidPayPayloadString(final @Nullable String payloadString) {
    //NO-OP
  }

  private boolean handleCheckoutThanksUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    final Intent intent = new Intent(this, ThanksActivity.class)
      .putExtra(IntentKey.PROJECT, this.project);
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
  public void externalLinkActivated(final @NotNull String url) {}

  @Override
  public void pageIntercepted(final @NotNull String url) {
    this.viewModel.inputs.pageIntercepted(url);
  }

  @Override
  public void onReceivedError(final @NotNull String url) {}

  @Override
  protected Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }
}
