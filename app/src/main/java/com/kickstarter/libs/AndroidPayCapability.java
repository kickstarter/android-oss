package com.kickstarter.libs;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.Wallet;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.utils.PlayServicesCapability;

import androidx.annotation.NonNull;

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
        this.isCapable = result.getStatus().isSuccess() && result.getValue();
        googleApiClient.disconnect();
      });
    } else {
      this.isCapable = false;
    }
  }

  public boolean isCapable() {
    return this.isCapable;
  }
}
