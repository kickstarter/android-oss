package com.kickstarter.libs.utils;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.InstrumentInfo;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.gson.Gson;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.models.AndroidPayAuthorizedPayload;
import com.kickstarter.libs.models.AndroidPayPayload;
import com.kickstarter.ui.data.ActivityResult;

import java.util.List;

import rx.Observable;

public final class AndroidPayUtils {
  private AndroidPayUtils() {}

  public static int environment(final @NonNull Build build) {
    return build.isInternal() ? WalletConstants.ENVIRONMENT_TEST : WalletConstants.ENVIRONMENT_PRODUCTION;
  }

  public static @NonNull MaskedWalletRequest createMaskedWalletRequest(final @NonNull AndroidPayPayload payload) {

    return MaskedWalletRequest.newBuilder()
      .setMerchantName(payload.merchantName())
      .setPhoneNumberRequired(payload.phoneNumberRequired())
      .setShippingAddressRequired(payload.shippingAddressRequired())
      .setCurrencyCode(payload.currencyCode())
      .setAllowDebitCard(payload.allowDebitCard())
      .setAllowPrepaidCard(payload.allowPrepaidCard())
      .setEstimatedTotalPrice(payload.estimatedTotalPrice())
      .setCart(
        Cart.newBuilder()
          .setCurrencyCode(payload.cart().currencyCode())
          .setTotalPrice(payload.cart().totalPrice())
          .setLineItems(lineItemsFromPayload(payload))
          .build()
      )
      .setPaymentMethodTokenizationParameters(
        PaymentMethodTokenizationParameters.newBuilder()
          .setPaymentMethodTokenizationType(PaymentMethodTokenizationType.PAYMENT_GATEWAY)
          .addParameter("gateway", "stripe")
          .addParameter("stripe:publishableKey", payload.stripePublishableKey())
          .addParameter("stripe:version", payload.stripeVersion())
          .build()
      )
      .build();
  }

  public static @NonNull FullWalletRequest createFullWalletRequest(final @NonNull String googleTransactionId,
    final @NonNull AndroidPayPayload payload) {

    return FullWalletRequest.newBuilder()
      .setGoogleTransactionId(googleTransactionId)
      .setCart(
        Cart.newBuilder()
          .setCurrencyCode(payload.cart().currencyCode())
          .setTotalPrice(payload.cart().totalPrice())
          .setLineItems(lineItemsFromPayload(payload))
          .build()
      )
      .build();
  }

  /**
   * Returns true if the activity result contains data for a full wallet.
   */
  public static boolean isFullWalletRequest(final @NonNull ActivityResult result) {
    final Intent intent = result.intent();
    return
      intent != null &&
      intent.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1) == -1 &&
      intent.hasExtra(WalletConstants.EXTRA_FULL_WALLET) &&
      result.resultCode() == Activity.RESULT_OK &&
      result.requestCode() == ActivityRequestCodes.CHECKOUT_ACTIVITY_WALLET_OBTAINED_FULL;
  }

  public static boolean isMaskedWalletRequest(final @NonNull ActivityResult result) {
    final Intent intent = result.intent();
    return
      intent != null &&
      intent.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1) == -1 &&
      result.requestCode() == ActivityRequestCodes.CHECKOUT_ACTIVITY_WALLET_REQUEST ||
      result.requestCode() == ActivityRequestCodes.CHECKOUT_ACTIVITY_WALLET_CHANGE_REQUEST;
  }

  /**
   * Returns the error code contained in a wallet request. If no such error is found, `null` is returned.
   */
  public static Integer walletRequestError(final @NonNull ActivityResult result) {

    final Intent intent = result.intent();
    final int error = intent == null ? -1 : intent.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
    return error == -1 ? null : error;
  }

  /**
   * Constructs an authorized payload that can be sent back to our server from an Android Pay full wallet.
   */
  public static @NonNull AndroidPayAuthorizedPayload authorizedPayloadFromFullWallet(final @NonNull FullWallet fullWallet, final @NonNull Gson gson) {

    final InstrumentInfo instrumentInfo = fullWallet.getInstrumentInfos()[0];

    return AndroidPayAuthorizedPayload
      .builder()
      .androidPayWallet(
        AndroidPayAuthorizedPayload.AndroidPayWallet
          .builder()
          .googleTransactionId(fullWallet.getGoogleTransactionId())
          .instrumentDetails(instrumentInfo.getInstrumentDetails())
          .instrumentType(instrumentInfo.getInstrumentType())
          .build()
      )
      .stripeToken(AndroidPayAuthorizedPayload.create(fullWallet.getPaymentMethodToken().getToken(), gson))
      .build();
  }

  /**
   * Attempts to programmatically trigger an Android Pay sheet from a wallet fragment. It does this by crawling the
   * subviews of the fragment and clicking them.
   *
   * NB: This is very hacky.
   */
  public static void triggerAndroidPaySheet(final @NonNull SupportWalletFragment walletFragment) {
    try {
      final ViewGroup group = (ViewGroup) walletFragment.getView();
      if (group != null) {
        recursiveClickFirstChildView(group);
      }
    } catch (ClassCastException | NullPointerException ignored) {
    }
  }

  /**
   * Recursive crawls the view hierarchy of `viewGroup` in order to find a clickable child and click it.
   */
  private static boolean recursiveClickFirstChildView(final @NonNull ViewGroup viewGroup) {
    try {
      boolean continueRecursing = true;

      for (int idx = 0; idx < viewGroup.getChildCount() && continueRecursing; idx++) {
        final View child = viewGroup.getChildAt(idx);
        if (child.hasOnClickListeners()) {
          child.performClick();
          return false;
        } else {
          continueRecursing = recursiveClickFirstChildView((ViewGroup) child);
        }
      }
    } catch (ClassCastException | NullPointerException ignored) {
    }

    return true;
  }

  private static @NonNull List<LineItem> lineItemsFromPayload(final @NonNull AndroidPayPayload payload) {
    return Observable.from(payload.cart().lineItems())
      .map(AndroidPayUtils::lineItemFromPayloadLineItem)
      .toList().toBlocking().first();
  }

  private static @NonNull LineItem lineItemFromPayloadLineItem(final @NonNull AndroidPayPayload.Cart.LineItem payloadLineItem) {
    return LineItem.newBuilder()
      .setCurrencyCode(payloadLineItem.currencyCode())
      .setDescription(payloadLineItem.description())
      .setQuantity(payloadLineItem.quantity())
      .setTotalPrice(payloadLineItem.totalPrice())
      .setUnitPrice(payloadLineItem.unitPrice())
      .setRole(LineItem.Role.REGULAR)
      .build();
  }

  /**
   * Tries to parse a payload string into a {@link AndroidPayPayload} object.
   * @param payloadString An (optional) string of JSON that represents the payload.
   * @return The parsed {@link AndroidPayPayload} object if successful, and `null` otherwise.
   */
  public static @Nullable AndroidPayPayload payloadFromString(final @Nullable String payloadString, final @NonNull Gson gson) {
    if (payloadString == null) {
      return null;
    }

    final String json = new String(Base64.decode(payloadString, Base64.DEFAULT));
    return gson.fromJson(json, AndroidPayPayload.class);
  }
}
