package com.kickstarter.libs;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.Wallet;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.utils.PlayServicesCapability;

public final class AndroidPayCapability {
  private boolean isCapable;

  public AndroidPayCapability(final boolean isCapable) {
    this.isCapable = isCapable;
  }

  public AndroidPayCapability(final @NonNull PlayServicesCapability playServicesCapability,
    final @ApplicationContext @NonNull Context applicationContext) {

    if (playServicesCapability.isCapable()) {
      final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(applicationContext)
        .addApi(Wallet.API, new Wallet.WalletOptions.Builder().build())
        .build();

      googleApiClient.connect();
      Wallet.Payments.isReadyToPay(googleApiClient).setResultCallback(result -> {
        isCapable = result.getStatus().isSuccess() && result.getValue();
        googleApiClient.disconnect();
      });
    } else {
      isCapable = false;
    }
  }

  public boolean isCapable() {
    return isCapable;
  }
}
