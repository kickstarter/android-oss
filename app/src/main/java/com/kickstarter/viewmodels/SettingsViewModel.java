package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.UserUtils;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.activities.SettingsActivity;
import com.kickstarter.ui.data.Newsletter;
import com.kickstarter.viewmodels.errors.SettingsViewModelErrors;
import com.kickstarter.viewmodels.inputs.SettingsViewModelInputs;
import com.kickstarter.viewmodels.outputs.SettingsViewModelOutputs;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public class SettingsViewModel extends ActivityViewModel<SettingsActivity> implements SettingsViewModelInputs,
  SettingsViewModelErrors, SettingsViewModelOutputs {

  // INPUTS
  private final PublishSubject<Void> contactEmailClicked = PublishSubject.create();
  private final PublishSubject<Pair<Boolean, Newsletter>> newsletterInput = PublishSubject.create();
  private final PublishSubject<User> userInput = PublishSubject.create();
  public void logoutClicked() {
    this.showConfirmLogoutPrompt.onNext(true);
  }
  public void closeLogoutConfirmationClicked() {
    this.showConfirmLogoutPrompt.onNext(false);
  }
  private final PublishSubject<Void> confirmLogoutClicked = PublishSubject.create();
  @Override
  public void confirmLogoutClicked() {
    this.confirmLogoutClicked.onNext(null);
  }

  // OUTPUTS
  private final PublishSubject<Newsletter> showOptInPrompt = PublishSubject.create();
  public @NonNull Observable<Newsletter> showOptInPrompt() {
    return this.showOptInPrompt;
  }
  private final PublishSubject<Void> updateSuccess = PublishSubject.create();
  public @NonNull final Observable<Void> updateSuccess() {
    return this.updateSuccess;
  }
  private final BehaviorSubject<User> userOutput = BehaviorSubject.create();
  public @NonNull Observable<User> user() {
    return this.userOutput;
  }
  private final BehaviorSubject<Boolean> showConfirmLogoutPrompt = BehaviorSubject.create();
  public @NonNull Observable<Boolean> showConfirmLogoutPrompt() {
    return this.showConfirmLogoutPrompt;
  }
  private final BehaviorSubject<Void> logout = BehaviorSubject.create();
  @Override
  public @NonNull Observable<Void> logout() {
    return this.logout;
  }
  // ERRORS
  private final PublishSubject<Throwable> unableToSavePreferenceError = PublishSubject.create();
  public @NonNull final Observable<String> unableToSavePreferenceError() {
    return this.unableToSavePreferenceError
      .takeUntil(this.updateSuccess)
      .map(__ -> null);
  }

  public final SettingsViewModelInputs inputs = this;
  public final SettingsViewModelOutputs outputs = this;
  public final SettingsViewModelErrors errors = this;

  private final ApiClientType client;
  private final CurrentUserType currentUser;

  @Override
  public void contactEmailClicked() {
    this.contactEmailClicked.onNext(null);
  }

  @Override
  public void notifyMobileOfFollower(final boolean b) {
    this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyMobileOfFollower(b).build());
  }

  @Override
  public void notifyMobileOfFriendActivity(final boolean b) {
    this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyMobileOfFriendActivity(b).build());
  }

  @Override
  public void notifyMobileOfUpdates(final boolean b) {
    this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyMobileOfUpdates(b).build());
  }

  @Override
  public void notifyOfFollower(final boolean b) {
    this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyOfFollower(b).build());
  }

  @Override
  public void notifyOfFriendActivity(final boolean b) {
    this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyOfFriendActivity(b).build());
  }

  @Override
  public void notifyOfUpdates(final boolean b) {
    this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyOfUpdates(b).build());
  }

  @Override
  public void sendGamesNewsletter(final boolean checked) {
    this.userInput.onNext(this.userOutput.getValue().toBuilder().gamesNewsletter(checked).build());
    this.newsletterInput.onNext(new Pair<>(checked, Newsletter.GAMES));
  }

  @Override
  public void sendHappeningNewsletter(final boolean checked) {
    this.userInput.onNext(this.userOutput.getValue().toBuilder().happeningNewsletter(checked).build());
    this.newsletterInput.onNext(new Pair<>(checked, Newsletter.HAPPENING));
  }

  @Override
  public void sendPromoNewsletter(final boolean checked) {
    this.userInput.onNext(this.userOutput.getValue().toBuilder().promoNewsletter(checked).build());
    this.newsletterInput.onNext(new Pair<>(checked, Newsletter.PROMO));
  }

  @Override
  public void sendWeeklyNewsletter(final boolean checked) {
    this.userInput.onNext(this.userOutput.getValue().toBuilder().weeklyNewsletter(checked).build());
    this.newsletterInput.onNext(new Pair<>(checked, Newsletter.WEEKLY));
  }

  public SettingsViewModel(final @NonNull Environment environment) {
    super(environment);

    this.client = environment.apiClient();
    this.currentUser = environment.currentUser();

    this.client.fetchCurrentUser()
      .retry(2)
      .compose(Transformers.neverError())
      .compose(bindToLifecycle())
      .subscribe(this.currentUser::refresh);

    this.currentUser.observable()
      .take(1)
      .compose(bindToLifecycle())
      .subscribe(this.userOutput::onNext);

    this.userInput
      .concatMap(this::updateSettings)
      .compose(bindToLifecycle())
      .subscribe(this::success);

    this.userInput
      .compose(bindToLifecycle())
      .subscribe(this.userOutput);

    this.userOutput
      .window(2, 1)
      .flatMap(Observable::toList)
      .map(ListUtils::first)
      .compose(takeWhen(this.unableToSavePreferenceError))
      .compose(bindToLifecycle())
      .subscribe(this.userOutput);

    this.currentUser.observable()
      .compose(takePairWhen(this.newsletterInput))
      .filter(us -> requiresDoubleOptIn(us.first, us.second.first))
      .map(us -> us.second.second)
      .compose(bindToLifecycle())
      .subscribe(this.showOptInPrompt);

    this.contactEmailClicked
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackContactEmailClicked());

    this.newsletterInput
      .map(bs -> bs.first)
      .compose(bindToLifecycle())
      .subscribe(this.koala::trackNewsletterToggle);

    this.confirmLogoutClicked
      .compose(bindToLifecycle())
      .subscribe(__ -> {
        this.koala.trackLogout();
        this.logout.onNext(null);
      });

    this.koala.trackSettingsView();
  }

  private boolean requiresDoubleOptIn(final @NonNull User user, final boolean checked) {
    return UserUtils.isLocationGermany(user) && checked;
  }

  private void success(final @NonNull User user) {
    this.currentUser.refresh(user);
    this.updateSuccess.onNext(null);
  }

  private @NonNull Observable<User> updateSettings(final @NonNull User user) {
    return this.client.updateUserSettings(user)
      .compose(Transformers.pipeErrorsTo(this.unableToSavePreferenceError));
  }
}
