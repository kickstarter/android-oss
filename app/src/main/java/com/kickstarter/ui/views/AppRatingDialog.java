package com.kickstarter.ui.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialog;
import android.widget.Button;

import com.kickstarter.R;
import com.kickstarter.libs.utils.ViewUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppRatingDialog extends AppCompatDialog {
  protected @Bind(R.id.no_thanks_button) Button noThanksButton;
  protected @Bind(R.id.remind_button) Button remindButton;
  protected @Bind(R.id.rate_button) Button rateButton;

  public AppRatingDialog(final @NonNull Context context) {
    super(context);
  }

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    setContentView(R.layout.app_rating_prompt);
    ButterKnife.bind(this);
  }

  @OnClick(R.id.rate_button)
  protected void rateButtonClick() {
    dismiss();
    // store pref to never show again
    ViewUtils.openStoreRating(getContext(), getContext().getPackageName());
  }

  @OnClick(R.id.remind_button)
  protected void remindButtonClick() {
    dismiss();
  }

  @OnClick(R.id.no_thanks_button)
  protected void noThanksButtonClick() {
    dismiss();
    // store pref to never show again
  }
}
