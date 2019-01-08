package com.kickstarter.libs;

import android.content.Intent;

import com.kickstarter.R;
import com.kickstarter.ui.activities.InternalToolsActivity;

import androidx.annotation.NonNull;

public final class InternalTools implements InternalToolsType {
  @Override
  public void maybeStartInternalToolsActivity(final @NonNull BaseActivity baseActivity) {
    final Intent intent = new Intent(baseActivity, InternalToolsActivity.class);
    baseActivity.startActivity(intent);
    baseActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  public @NonNull String basicAuthorizationHeader() {
    return "Basic ZnV6enk6d3V6enk=";
  }
}
