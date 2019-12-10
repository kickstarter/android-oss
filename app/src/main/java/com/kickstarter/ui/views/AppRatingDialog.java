package com.kickstarter.ui.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Koala;
import com.kickstarter.libs.preferences.BooleanPreferenceType;
import com.kickstarter.libs.qualifiers.AppRatingPreference;
import com.kickstarter.libs.qualifiers.KoalaTracker;
import com.kickstarter.libs.utils.ViewUtils;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppRatingDialog extends AppCompatDialog {
  protected @Inject @AppRatingPreference BooleanPreferenceType hasSeenAppRatingPreference;
  protected @Inject @KoalaTracker Koala koala;

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

    ((KSApplication) getContext().getApplicationContext()).component().inject(this);
  }

  @OnClick(R.id.rate_button)
  protected void rateButtonClick() {
    this.koala.trackAppRatingNow();
    this.hasSeenAppRatingPreference.set(true);
    dismiss();
    ViewUtils.openStoreRating(getContext(), getContext().getPackageName());
  }

  @OnClick(R.id.remind_button)
  protected void remindButtonClick() {
    this.koala.trackAppRatingRemindLater();
    dismiss();
  }

  @OnClick(R.id.no_thanks_button)
  protected void noThanksButtonClick() {
    this.koala.trackAppRatingNoThanks();
    this.hasSeenAppRatingPreference.set(true);
    dismiss();
  }
}
