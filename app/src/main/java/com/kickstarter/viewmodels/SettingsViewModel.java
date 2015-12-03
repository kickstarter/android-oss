package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.SettingsActivity;
import com.kickstarter.viewmodels.inputs.SettingsViewModelInputs;

import javax.inject.Inject;

import rx.subjects.PublishSubject;

public class SettingsViewModel extends ViewModel<SettingsActivity> implements SettingsViewModelInputs {

  // INPUTS
  private final PublishSubject<Boolean> sendHappeningNewsletter = PublishSubject.create();
  private final PublishSubject<Boolean> sendPromoNewsletter = PublishSubject.create();
  private final PublishSubject<Boolean> sendWeeklyNewsletter = PublishSubject.create();

  final SettingsViewModelInputs inputs = this;

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  @Override
  public void sendHappeningNewsletter(final boolean b) {
    sendHappeningNewsletter.onNext(b);
  }

  @Override
  public void sendPromoNewsletter(final boolean b) {
    sendPromoNewsletter.onNext(b);
  }

  @Override
  public void sendWeeklyNewsletter(final boolean b) {
    sendWeeklyNewsletter.onNext(b);
  }

  public SettingsViewModel() {


  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }
}
