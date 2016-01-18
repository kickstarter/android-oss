package com.kickstarter.libs;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.ui.activities.InternalToolsActivity;

public final class InternalTools implements InternalToolsType {
  @Override
  public void maybeStartDebugActivity(final @NonNull BaseActivity baseActivity) {
    final Intent intent = new Intent(baseActivity, InternalToolsActivity.class);
    baseActivity.startActivity(intent);
    baseActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
