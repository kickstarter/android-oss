package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.SettingsActivity;
import com.kickstarter.viewmodels.errors.SettingsViewModelErrors;
import com.kickstarter.viewmodels.inputs.SettingsViewModelInputs;
import com.kickstarter.viewmodels.outputs.SettingsViewModelOutputs;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class SettingsViewModel extends ViewModel<SettingsActivity> implements SettingsViewModelInputs,
  SettingsViewModelErrors, SettingsViewModelOutputs {

  // OUTPUTS
  private final BehaviorSubject<User> user = BehaviorSubject.create();
  public Observable<User> user() {
    return user;
  }

  // ERRORS
  private final PublishSubject<Throwable> unableToSavePreferenceError = PublishSubject.create();
  public final Observable<String> unableToSavePreferenceError() {
    return unableToSavePreferenceError.map(__ -> null);
  }

  public final SettingsViewModelInputs inputs = this;
  public final SettingsViewModelOutputs outputs = this;
  public final SettingsViewModelErrors errors = this;

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  @Override
  public void notifyMobileOfFollower(final boolean b) {
    user.onNext(user.getValue().toBuilder().notifyMobileOfFollower(b).build());
  }

  @Override
  public void notifyMobileOfFriendActivity(final boolean b) {
    user.onNext(user.getValue().toBuilder().notifyMobileOfFriendActivity(b).build());
  }

  @Override
  public void notifyMobileOfUpdates(final boolean b) {
    user.onNext(user.getValue().toBuilder().notifyMobileOfUpdates(b).build());
  }

  @Override
  public void notifyOfFollower(final boolean b) {
    user.onNext(user.getValue().toBuilder().notifyOfFollower(b).build());
  }

  @Override
  public void notifyOfFriendActivity(final boolean b) {
    user.onNext(user.getValue().toBuilder().notifyOfFriendActivity(b).build());
  }

  @Override
  public void notifyOfUpdates(final boolean b) {
    user.onNext(user.getValue().toBuilder().notifyOfUpdates(b).build());
  }

  @Override
  public void sendHappeningNewsletter(final boolean b) {
    user.onNext(user.getValue().toBuilder().happeningNewsletter(b).build());
  }

  @Override
  public void sendPromoNewsletter(final boolean b) {
    user.onNext(user.getValue().toBuilder().promoNewsletter(b).build());
  }

  @Override
  public void sendWeeklyNewsletter(final boolean b) {
    user.onNext(user.getValue().toBuilder().weeklyNewsletter(b).build());
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<User> freshUser = client.fetchCurrentUser()
      .retry(2)
      .onErrorResumeNext(e -> Observable.empty());

    freshUser.subscribe(currentUser::refresh);

    currentUser.observable()
      .take(1)
      .subscribe(user::onNext);

    user
      .skip(1)
      .concatMap(client::updateUserSettings)
      .compose(Transformers.pipeErrorsTo(unableToSavePreferenceError))
      .subscribe(currentUser::refresh);

    user
      .window(2, 1)
      .flatMap(Observable::toList)
      .compose(Transformers.takeWhen(unableToSavePreferenceError))
      .map(ListUtils::first)
      .subscribe(user);
  }
}
