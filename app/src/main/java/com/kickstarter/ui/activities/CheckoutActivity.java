package com.kickstarter.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Pair;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.gson.Gson;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.AndroidPayCapability;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.models.AndroidPayAuthorizedPayload;
import com.kickstarter.libs.models.AndroidPayPayload;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.AndroidPayUtils;
import com.kickstarter.libs.utils.AnimationUtils;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.ui.toolbars.KSToolbar;
import com.kickstarter.ui.views.ConfirmDialog;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.CheckoutViewModel;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Request;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(CheckoutViewModel.class)
public final class CheckoutActivity extends BaseActivity<CheckoutViewModel> implements KSWebViewClient.Delegate {
  private @Nullable Project project;

  protected @Bind(R.id.checkout_toolbar) KSToolbar checkoutToolbar;
  protected @Bind(R.id.web_view) KSWebView webView;
  protected @Bind(R.id.checkout_loading_indicator) View loadingIndicatorView;
  protected @Bind(R.id.confirmation_group) View confirmationGroup;
  protected @Bind(R.id.pledge_disclaimer) TextView pledgeDisclaimerTextView;
  protected @Bind(R.id.terms_and_privacy) TextView termsAndPrivacyTextView;
  protected @Bind(R.id.backer_101) TextView backer101TextView;

  // Android pay summary bindings
  protected @Bind(R.id.android_pay_instrument_description) TextView androidPayInstrumentDescriptionTextView;
  protected @Bind(R.id.android_pay_email) TextView androidPayEmailTextView;

  // Project context view bindings
  protected @Bind(R.id.project_context_image_view) ImageView contextPhotoImageView;
  protected @Bind(R.id.project_context_creator_name) TextView creatorNameTextView;
  protected @Bind(R.id.project_context_project_name) TextView projectNameTextView;

  protected @BindString(R.string.profile_settings_about_terms) String termsOfUseString;
  protected @BindString(R.string.profile_settings_about_privacy) String privacyPolicyString;
  protected @BindString(R.string.project_checkout_android_pay_pledge_disclaimer) String pledgeDisclaimerString;
  protected @BindString(R.string.project_checkout_android_pay_terms_and_privacy) String termsAndPrivacyString;
  protected @BindString(R.string.project_checkout_android_pay_backer_101) String backer101String;
  protected @BindString(R.string.project_creator_by_creator) String projectCreatorByCreatorString;
  protected @BindString(R.string.project_checkout_android_pay_error_title) String androidPayErrorTitleString;
  protected @BindString(R.string.project_checkout_android_pay_error_message) String androidPayErrorMessageString;

  protected @BindColor(R.color.white) int whiteColor;

  protected @Inject KSCurrency ksCurrency;
  protected @Inject KSString ksString;
  protected @Inject Gson gson;
  protected @Inject AndroidPayCapability androidPayCapability;
  protected @Inject Build build;

  private @Nullable SupportWalletFragment walletFragment;
  private @Nullable SupportWalletFragment confirmationWalletFragment;
  private @Nullable GoogleApiClient googleApiClient;
  private boolean isInAndroidPayFlow;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.checkout_layout);
    ButterKnife.bind(this);

    ((KSApplication) getApplication()).component().inject(this);

    webView.client().setDelegate(this);

    webView.client().registerRequestHandlers(Arrays.asList(
      new RequestHandler(KSUri::isCheckoutThanksUri, this::handleCheckoutThanksUriRequest),
      new RequestHandler(KSUri::isSignupUri, this::handleSignupUriRequest)
    ));

    viewModel.outputs.project()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(project -> this.project = project);

    viewModel.outputs.title()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(checkoutToolbar::setTitle);

    viewModel.outputs.isAndroidPayAvailable()
      .filter(BooleanUtils::isTrue)
      .take(1)
      .subscribe(__ -> setGoogleApiClient());

    viewModel.outputs.isAndroidPayAvailable()
      .filter(BooleanUtils::isTrue)
      .take(1)
      .subscribe(__ -> this.prepareWalletFragment());

    viewModel.outputs.showAndroidPaySheet()
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showAndroidPaySheet);

    viewModel.outputs.displayAndroidPayConfirmation()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showOrHideAndroidPayConfirmation);

    viewModel.outputs.updateAndroidPayConfirmation()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(wp -> updateAndroidPayConfirmation(wp.first, wp.second));

    viewModel.outputs.popActivityOffStack()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> this.popActivity());

    viewModel.outputs.attemptAndroidPayConfirmation()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(wp -> this.attemptAndroidPayConfirmation(wp.first, wp.second));

    viewModel.outputs.completeAndroidPay()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::androidPayComplete);

    viewModel.errors.androidPayError()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showAndroidPayError);
  }

  @Override
  protected void onResume() {
    super.onResume();

    viewModel.outputs.url()
      .take(1)
      .filter(__ -> !isInAndroidPayFlow)
      .subscribe(webView::loadUrl);
  }

  @Override
  public void back() {
    isInAndroidPayFlow = false;
    viewModel.inputs.backButtonClicked();
  }

  private void popActivity() {
    super.back();
  }

  /**
   * This method is called from {@link com.kickstarter.services.KSWebViewClient} when an Android Pay
   * payload has been obtained from the webview.
   */
  public void takeAndroidPayPayloadString(final @Nullable String payloadString) {
    viewModel.inputs.takePayloadString(payloadString);
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

  /**
   * Call when the android pay sheet should be shown.
   */
  private void showAndroidPaySheet(final @NonNull AndroidPayPayload payload) {
    if (walletFragment == null) {
      return;
    }

    isInAndroidPayFlow = true;

    final MaskedWalletRequest request = AndroidPayUtils.createMaskedWalletRequest(payload);
    walletFragment.initialize(
      WalletFragmentInitParams.newBuilder()
        .setMaskedWalletRequest(request)
        .setMaskedWalletRequestCode(ActivityRequestCodes.CHECKOUT_ACTIVITY_WALLET_REQUEST)
        .build()
    );

    AndroidPayUtils.triggerAndroidPaySheet(walletFragment);
  }

  /**
   * Creates and injects a wallet fragment into the activity.
   */
  private void prepareWalletFragment() {
    final WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
      .setEnvironment(AndroidPayUtils.environment(build))
      .setTheme(WalletConstants.THEME_LIGHT)
      .setMode(WalletFragmentMode.BUY_BUTTON)
      .build();

    walletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);

    getSupportFragmentManager().beginTransaction()
      .replace(R.id.masked_wallet_fragment, walletFragment)
      .commit();
  }

  private void setGoogleApiClient() {
    googleApiClient = new GoogleApiClient.Builder(this)
      .enableAutoManage(this, null)
      .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
        .setEnvironment(AndroidPayUtils.environment(build))
        .setTheme(WalletConstants.THEME_LIGHT)
        .build())
      .build();
  }

  private void showOrHideAndroidPayConfirmation(final boolean visible) {
    if (visible) {
      webView.setVisibility(View.GONE);
      confirmationGroup.setVisibility(View.VISIBLE);
    } else {
      webView.setVisibility(View.VISIBLE);
      confirmationGroup.setVisibility(View.GONE);
    }
  }

  /**
   * Call when a masked wallet has been obtained and the content in the android pay confirmation should be rendered.
   */
  private void updateAndroidPayConfirmation(final @NonNull MaskedWallet maskedWallet,
    final @NonNull AndroidPayPayload payload) {

    Picasso.with(this).load(project.photo().full()).into(contextPhotoImageView);
    projectNameTextView.setText(project.name());
    creatorNameTextView.setText(ksString.format(
      projectCreatorByCreatorString,
      "creator_name",
      project.creator().name()
    ));

    termsAndPrivacyTextView.setText(Html.fromHtml(termsAndPrivacyString));
    backer101TextView.setText(Html.fromHtml(backer101String));
    if (maskedWallet != null) {
      androidPayEmailTextView.setText(maskedWallet.getEmail());
      final String[] paymentDescriptions = maskedWallet.getPaymentDescriptions();
      if (paymentDescriptions.length > 0) {
        androidPayInstrumentDescriptionTextView.setText(paymentDescriptions[0]);
      }
    }

    pledgeDisclaimerTextView.setText(Html.fromHtml(
      ksString.format(pledgeDisclaimerString,
        "charge_amount", ksCurrency.format(
          Float.valueOf(payload.cart().totalPrice()),
          project
        )
      )
    ));

    confirmationWalletFragment = SupportWalletFragment.newInstance(
      WalletFragmentOptions.newBuilder()
        .setEnvironment(AndroidPayUtils.environment(build))
        .setTheme(WalletConstants.THEME_LIGHT)
        .setMode(WalletFragmentMode.SELECTION_DETAILS)
        .build()
    );

    confirmationWalletFragment.initialize(
      WalletFragmentInitParams.newBuilder()
        .setMaskedWallet(maskedWallet)
        .setMaskedWalletRequestCode(ActivityRequestCodes.CHECKOUT_ACTIVITY_WALLET_CHANGE_REQUEST)
        .build()
    );

    getSupportFragmentManager().beginTransaction()
      .replace(R.id.confirmation_masked_wallet_fragment, confirmationWalletFragment)
      .commit();
  }

  private void attemptAndroidPayConfirmation(final @NonNull MaskedWallet maskedWallet,
    final @NonNull AndroidPayPayload payload) {

    final FullWalletRequest fullWalletRequest = AndroidPayUtils.createFullWalletRequest(
      maskedWallet.getGoogleTransactionId(),
      payload
    );

    Wallet.Payments.loadFullWallet(googleApiClient, fullWalletRequest,
      ActivityRequestCodes.CHECKOUT_ACTIVITY_WALLET_OBTAINED_FULL);
  }

  @SuppressLint("NewApi")
  private void androidPayComplete(final @NonNull FullWallet fullWallet) {
    final AndroidPayAuthorizedPayload authorizedPayload = AndroidPayUtils.authorizedPayloadFromFullWallet(fullWallet, gson);

    final String json = gson.toJson(authorizedPayload, AndroidPayAuthorizedPayload.class);

    // TODO: is this an injection problem?
    final String javascript = String.format("checkout_android_pay_next(%s);", json);
    webView.evaluateJavascript(javascript, null);
  }

  private void showAndroidPayError(final @NonNull Integer error) {
    final ConfirmDialog dialog = new ConfirmDialog(this,
      androidPayErrorTitleString + " (" + error.toString() + ")",
      androidPayErrorMessageString);
    dialog.setOnDismissListener(d -> this.back());
    dialog.show();
  }

  @OnClick(R.id.back_button)
  protected void toolbarBackButtonClicked() {
    viewModel.inputs.backButtonClicked();
  }

  @OnClick(R.id.android_pay_confirmation_button)
  protected void androidPayConfirmationClicked() {
    viewModel.inputs.confirmAndroidPayClicked();
  }

  @OnClick(R.id.android_pay_change)
  protected void androidPayChangeClicked() {
    if (confirmationWalletFragment != null) {
      AndroidPayUtils.triggerAndroidPaySheet(confirmationWalletFragment);
    }
  }

  @OnClick(R.id.terms_and_privacy)
  protected void termsAndPrivacyClicked() {
    final CharSequence[] items = new CharSequence[] {
      termsOfUseString,
      privacyPolicyString
    };
    new AlertDialog.Builder(this)
      .setItems(items, (__, which) -> {
        final Intent intent;
        if (which == 0) {
          intent = new Intent(this, HelpActivity.Terms.class);
        } else {
          intent = new Intent(this, HelpActivity.Privacy.class);
        }
        startActivityWithTransition(intent, 
          R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
      })
      .show();
  }

  @OnClick(R.id.backer_101)
  protected void backer101Clicked() {
    final Intent intent = new Intent(this, WebViewActivity.class);
    intent.putExtra(IntentKey.URL, Uri.parse(project.webProjectUrl())
      .buildUpon()
      .appendEncodedPath("pledge/big_print")
      .build()
      .toString()
    );
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  public void webViewOnPageStarted(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {
    loadingIndicatorView.startAnimation(AnimationUtils.INSTANCE.appearAnimation());
  }

  @Override
  public void webViewOnPageFinished(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {
    loadingIndicatorView.startAnimation(AnimationUtils.INSTANCE.disappearAnimation());
  }

  @Override
  public void webViewPageIntercepted(final @NonNull KSWebViewClient webViewClient, final @NonNull String url) {
    viewModel.inputs.pageIntercepted(url);
  }

  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }
}
