package com.kickstarter.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.ui.toolbars.KSToolbar;
import com.kickstarter.ui.views.ConfirmDialog;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.CheckoutViewModel;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Request;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(CheckoutViewModel.ViewModel.class)
public final class CheckoutActivity extends BaseActivity<CheckoutViewModel.ViewModel> implements KSWebView.Delegate {
  private @Nullable Project project;

  protected @Bind(R.id.checkout_toolbar) KSToolbar checkoutToolbar;
  protected @Bind(R.id.web_view) KSWebView webView;
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

  private KSCurrency ksCurrency;
  private KSString ksString;
  private Gson gson;
  private AndroidPayCapability androidPayCapability;
  private Build build;

  private @Nullable SupportWalletFragment walletFragment;
  private @Nullable SupportWalletFragment confirmationWalletFragment;
  private @Nullable GoogleApiClient googleApiClient;
  private boolean isInAndroidPayFlow;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.checkout_layout);
    ButterKnife.bind(this);

    this.androidPayCapability = environment().androidPayCapability();
    this.build = environment().build();
    this.ksCurrency = environment().ksCurrency();
    this.ksString = environment().ksString();
    this.gson = environment().gson();

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

    this.viewModel.outputs.isAndroidPayAvailable()
      .filter(BooleanUtils::isTrue)
      .take(1)
      .subscribe(__ -> setGoogleApiClient());

    this.viewModel.outputs.isAndroidPayAvailable()
      .filter(BooleanUtils::isTrue)
      .take(1)
      .subscribe(__ -> this.prepareWalletFragment());

    this.viewModel.outputs.showAndroidPaySheet()
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::showAndroidPaySheet);

    this.viewModel.outputs.displayAndroidPayConfirmation()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::showOrHideAndroidPayConfirmation);

    this.viewModel.outputs.updateAndroidPayConfirmation()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(wp -> updateAndroidPayConfirmation(wp.first, wp.second));

    this.viewModel.outputs.popActivityOffStack()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.popActivity());

    this.viewModel.outputs.attemptAndroidPayConfirmation()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(wp -> this.attemptAndroidPayConfirmation(wp.first, wp.second));

    this.viewModel.outputs.completeAndroidPay()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::androidPayComplete);

    this.viewModel.outputs.androidPayError()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::showAndroidPayError);
  }

  @Override
  protected void onResume() {
    super.onResume();

    this.viewModel.outputs.url()
      .take(1)
      .filter(__ -> !this.isInAndroidPayFlow)
      .subscribe(this.webView::loadUrl);
  }

  @Override
  public void back() {
    this.isInAndroidPayFlow = false;
    this.viewModel.inputs.backButtonClicked();
  }

  private void popActivity() {
    super.back();
  }

  /**
   * This method is called from {@link com.kickstarter.services.KSWebViewClient} when an Android Pay
   * payload has been obtained from the webview.
   */
  public void takeAndroidPayPayloadString(final @Nullable String payloadString) {
    this.viewModel.inputs.takePayloadString(payloadString);
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

  /**
   * Call when the android pay sheet should be shown.
   */
  private void showAndroidPaySheet(final @NonNull AndroidPayPayload payload) {
    if (this.walletFragment == null) {
      return;
    }

    this.isInAndroidPayFlow = true;

    final MaskedWalletRequest request = AndroidPayUtils.createMaskedWalletRequest(payload);
    this.walletFragment.initialize(
      WalletFragmentInitParams.newBuilder()
        .setMaskedWalletRequest(request)
        .setMaskedWalletRequestCode(ActivityRequestCodes.CHECKOUT_ACTIVITY_WALLET_REQUEST)
        .build()
    );

    AndroidPayUtils.triggerAndroidPaySheet(this.walletFragment);
  }

  /**
   * Creates and injects a wallet fragment into the activity.
   */
  private void prepareWalletFragment() {
    final WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
      .setEnvironment(AndroidPayUtils.environment(this.build))
      .setTheme(WalletConstants.THEME_LIGHT)
      .setMode(WalletFragmentMode.BUY_BUTTON)
      .build();

    this.walletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);

    getSupportFragmentManager().beginTransaction()
      .replace(R.id.masked_wallet_fragment, this.walletFragment)
      .commit();
  }

  private void setGoogleApiClient() {
    this.googleApiClient = new GoogleApiClient.Builder(this)
      .enableAutoManage(this, null)
      .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
        .setEnvironment(AndroidPayUtils.environment(this.build))
        .setTheme(WalletConstants.THEME_LIGHT)
        .build())
      .build();
  }

  private void showOrHideAndroidPayConfirmation(final boolean visible) {
    if (visible) {
      this.webView.setVisibility(View.GONE);
      this.confirmationGroup.setVisibility(View.VISIBLE);
    } else {
      this.webView.setVisibility(View.VISIBLE);
      this.confirmationGroup.setVisibility(View.GONE);
    }
  }

  /**
   * Call when a masked wallet has been obtained and the content in the android pay confirmation should be rendered.
   */
  private void updateAndroidPayConfirmation(final @NonNull MaskedWallet maskedWallet,
    final @NonNull AndroidPayPayload payload) {

    Picasso.with(this).load(this.project.photo().full()).into(this.contextPhotoImageView);
    this.projectNameTextView.setText(this.project.name());
    this.creatorNameTextView.setText(this.ksString.format(
      this.projectCreatorByCreatorString,
      "creator_name",
      this.project.creator().name()
    ));

    this.termsAndPrivacyTextView.setText(Html.fromHtml(this.termsAndPrivacyString));
    this.backer101TextView.setText(Html.fromHtml(this.backer101String));
    if (maskedWallet != null) {
      this.androidPayEmailTextView.setText(maskedWallet.getEmail());
      final String[] paymentDescriptions = maskedWallet.getPaymentDescriptions();
      if (paymentDescriptions.length > 0) {
        this.androidPayInstrumentDescriptionTextView.setText(paymentDescriptions[0]);
      }
    }

    this.pledgeDisclaimerTextView.setText(Html.fromHtml(
      this.ksString.format(
        this.pledgeDisclaimerString,
        "charge_amount",
        this.ksCurrency.format(Float.valueOf(payload.cart().totalPrice()), this.project, RoundingMode.HALF_UP)
      )
    ));

    this.confirmationWalletFragment = SupportWalletFragment.newInstance(
      WalletFragmentOptions.newBuilder()
        .setEnvironment(AndroidPayUtils.environment(this.build))
        .setTheme(WalletConstants.THEME_LIGHT)
        .setMode(WalletFragmentMode.SELECTION_DETAILS)
        .build()
    );

    this.confirmationWalletFragment.initialize(
      WalletFragmentInitParams.newBuilder()
        .setMaskedWallet(maskedWallet)
        .setMaskedWalletRequestCode(ActivityRequestCodes.CHECKOUT_ACTIVITY_WALLET_CHANGE_REQUEST)
        .build()
    );

    getSupportFragmentManager().beginTransaction()
      .replace(R.id.confirmation_masked_wallet_fragment, this.confirmationWalletFragment)
      .commit();
  }

  private void attemptAndroidPayConfirmation(final @NonNull MaskedWallet maskedWallet,
    final @NonNull AndroidPayPayload payload) {

    final FullWalletRequest fullWalletRequest = AndroidPayUtils.createFullWalletRequest(
      maskedWallet.getGoogleTransactionId(),
      payload
    );

    Wallet.Payments.loadFullWallet(this.googleApiClient, fullWalletRequest,
      ActivityRequestCodes.CHECKOUT_ACTIVITY_WALLET_OBTAINED_FULL);
  }

  @SuppressLint("NewApi")
  private void androidPayComplete(final @NonNull FullWallet fullWallet) {
    final AndroidPayAuthorizedPayload authorizedPayload = AndroidPayUtils
      .authorizedPayloadFromFullWallet(fullWallet, this.gson);

    final String json = this.gson.toJson(authorizedPayload, AndroidPayAuthorizedPayload.class);

    // TODO: is this an injection problem?
    final String javascript = String.format("checkout_android_pay_next(%s);", json);
    this.webView.evaluateJavascript(javascript, null);
  }

  private void showAndroidPayError(final @NonNull Integer error) {
    final ConfirmDialog dialog = new ConfirmDialog(this,
      this.androidPayErrorTitleString + " (" + error.toString() + ")",
      this.androidPayErrorMessageString);
    dialog.setOnDismissListener(d -> this.back());
    dialog.show();
  }

  @OnClick(R.id.back_button)
  protected void toolbarBackButtonClicked() {
    this.viewModel.inputs.backButtonClicked();
  }

  @OnClick(R.id.android_pay_confirmation_button)
  protected void androidPayConfirmationClicked() {
    this.viewModel.inputs.confirmAndroidPayClicked();
  }

  @OnClick(R.id.android_pay_change)
  protected void androidPayChangeClicked() {
    if (this.confirmationWalletFragment != null) {
      AndroidPayUtils.triggerAndroidPaySheet(this.confirmationWalletFragment);
    }
  }

  @OnClick(R.id.terms_and_privacy)
  protected void termsAndPrivacyClicked() {
    final CharSequence[] items = new CharSequence[] {
      this.termsOfUseString,
      this.privacyPolicyString
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
    intent.putExtra(IntentKey.URL, Uri.parse(this.project.webProjectUrl())
      .buildUpon()
      .appendEncodedPath("pledge/big_print")
      .build()
      .toString()
    );

    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  public void externalLinkActivated(@NotNull String url) {}

  @Override
  public void pageIntercepted(@NotNull String url) {
    this.viewModel.inputs.pageIntercepted(url);
  }

  @Override
  public void onReceivedError(@NotNull String url) {}

  @Override
  protected Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }
}
