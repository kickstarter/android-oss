package com.kickstarter.mock.factories;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.wallet.WalletConstants;
import com.kickstarter.ui.data.ActivityResult;

public final class ActivityResultFactory {
  private ActivityResultFactory() {}

  public static @NonNull ActivityResult activityResult() {
    return ActivityResult.builder()
      .requestCode(1)
      .resultCode(Activity.RESULT_OK)
      .build();
  }

  public static @NonNull ActivityResult androidPayErrorResult() {
    return activityResult()
      .toBuilder()
      .intent(new Intent().putExtra(WalletConstants.EXTRA_ERROR_CODE, 1))
      .build();
  }
}
