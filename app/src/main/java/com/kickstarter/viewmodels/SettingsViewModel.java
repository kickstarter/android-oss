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

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface SettingsViewModel {

  interface Inputs {
    /** Call when the user dismiss the logout confirmation dialog. */
    void closeLogoutConfirmationClicked();

    /** Call when the user has confirmed that they want to log out. */
    void confirmLogoutClicked();

    /** Call when the user clicks on contact email. */
    void contactEmailClicked();

    /** Call when the user clicks the Follwing info icon. */
    void followingInfoClicked();

    /** Call when the user taps the logout button. */
    void logoutClicked();

    /** Call when the notify mobile of new followers toggle changes. */
    void notifyMobileOfFollower(boolean checked);

    /** Call when the notify mobile of friend backs a project toggle changes. */
    void notifyMobileOfFriendActivity(boolean checked);

    /** Call when the notify mobile of messages toggle changes. */
    void notifyMobileOfMessages(boolean checked);

    /** Call when the notify mobile of project updates toggle changes. */
    void notifyMobileOfUpdates(boolean checked);

    /** Call when the notify of new followers toggle changes. */
    void notifyOfFollower(boolean checked);

    /** Call when the notify of friend backs a project toggle changes. */
    void notifyOfFriendActivity(boolean checked);

    /** Call when the notify of messages toggle changes. */
    void notifyOfMessages(boolean checked);

    /** Call when the notify of project updates toggle changes. */
    void notifyOfUpdates(boolean checked);

    /** Call when the user toggles the Following switch. */
    void optIntoFollowing(boolean checked);

    /** Call when the user confirms or cancels opting out of Following. */
    void optOutOfFollowing(boolean optOut);

    /** Call when the user toggles the Recommendations switch. */
    void optedOutOfRecommendations(boolean checked);

    /** Call when the user clicks the Recommendations info icon. */
    void recommendationsInfoClicked();

    /** Call when the user clicks the Recommendations info icon. */
    void privateProfileInfoClicked();

    /** Call when the user toggles the Kickstarter Loves Games newsletter switch. */
    void sendGamesNewsletter(boolean checked);

    /** Call when the user toggles the Happening newsletter switch. */
    void sendHappeningNewsletter(boolean checked);

    /** Call when the user toggles the Kickstarter News & Events newsletter switch. */
    void sendPromoNewsletter(boolean checked);

    /** Call when the user toggles the Projects We Love newsletter switch. */
    void sendWeeklyNewsletter(boolean checked);

    /** Call when user toggles the private profile switch. */
    void showPublicProfile(boolean checked);
  }

  interface Outputs {
    /** Emits when its time to log the user out. */
    Observable<Void> logout();

    /** Emits when Following switch should be turned back on after user cancels opting out. */
    Observable<Void> hideConfirmFollowingOptOutPrompt();

    /** Emits when user should be shown the Following confirmation dialog. */
    Observable<Void> showConfirmFollowingOptOutPrompt();

    /** Emits a boolean that determines if the logout confirmation should be displayed. */
    Observable<Boolean> showConfirmLogoutPrompt();

    /** Emits when user should be shown the Following info dialog. */
    Observable<Void> showFollowingInfo();

    /** Show a dialog to inform the user that their newsletter subscription must be confirmed via email. */
    Observable<Newsletter> showOptInPrompt();

    /** Emits when user should be shown the Recommendations info dialog. */
    Observable<Void> showRecommendationsInfo();

    /** Emits when user should be shown the Private Profile info dialog */
    Observable<Void> showPrivateProfileInfo();

    /** Emits user containing settings state. */
    Observable<User> user();
  }

  interface Errors {
    Observable<String> unableToSavePreferenceError();
  }

  final class ViewModel extends ActivityViewModel<SettingsActivity> implements Inputs, Errors, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
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

      this.optIntoFollowing
        .compose(bindToLifecycle())
        .filter(checked -> checked)
        .subscribe(__ -> this.userInput.onNext(this.userOutput.getValue().toBuilder().social(true).build()));

      this.optIntoFollowing
        .compose(bindToLifecycle())
        .filter(checked -> !checked)
        .subscribe(__ -> this.showConfirmFollowingOptOutPrompt.onNext(null));

      this.optOutOfFollowing
        .compose(bindToLifecycle())
        .filter(optOut -> optOut)
        .subscribe(__ -> this.userInput.onNext(this.userOutput.getValue().toBuilder().social(false).build()));

      this.optOutOfFollowing
        .compose(bindToLifecycle())
        .filter(optOut -> !optOut)
        .subscribe(__ -> this.hideConfirmFollowingOptOutPrompt.onNext(null));

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

    private final PublishSubject<Void> confirmLogoutClicked = PublishSubject.create();
    private final PublishSubject<Void> contactEmailClicked = PublishSubject.create();
    private final PublishSubject<Boolean> optedOutOfRecommendations = PublishSubject.create();
    private final PublishSubject<Pair<Boolean, Newsletter>> newsletterInput = PublishSubject.create();
    private final PublishSubject<Boolean> optIntoFollowing = PublishSubject.create();
    private final PublishSubject<Boolean> optOutOfFollowing = PublishSubject.create();
    private final PublishSubject<User> userInput = PublishSubject.create();

    private final BehaviorSubject<Void> hideConfirmFollowingOptOutPrompt = BehaviorSubject.create();
    private final BehaviorSubject<Void> logout = BehaviorSubject.create();
    private final BehaviorSubject<Void> showConfirmFollowingOptOutPrompt = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> showConfirmLogoutPrompt = BehaviorSubject.create();
    private final BehaviorSubject<Void> showFollowingInfo = BehaviorSubject.create();
    private final PublishSubject<Newsletter> showOptInPrompt = PublishSubject.create();
    private final PublishSubject<Void> showRecommendationsInfo = PublishSubject.create();
    private final PublishSubject<Void> showPrivateProfileInfo = PublishSubject.create();
    private final PublishSubject<Void> updateSuccess = PublishSubject.create();
    private final BehaviorSubject<User> userOutput = BehaviorSubject.create();

    private final PublishSubject<Throwable> unableToSavePreferenceError = PublishSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;
    public final Errors errors = this;

    @Override public void closeLogoutConfirmationClicked() {
      this.showConfirmLogoutPrompt.onNext(false);
    }
    @Override public void confirmLogoutClicked() {
      this.confirmLogoutClicked.onNext(null);
    }
    @Override public void contactEmailClicked() {
      this.contactEmailClicked.onNext(null);
    }
    @Override public void followingInfoClicked() {
      this.showFollowingInfo.onNext(null);
    }
    @Override
    public void optedOutOfRecommendations(final boolean checked) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().optedOutOfRecommendations(!checked).build());
      this.optedOutOfRecommendations.onNext(!checked);
    }
    @Override public void recommendationsInfoClicked() {
      this.showRecommendationsInfo.onNext(null);
    }
    @Override
    public void privateProfileInfoClicked() {
      this.showPrivateProfileInfo.onNext(null);
    }
    @Override public void logoutClicked() {
      this.showConfirmLogoutPrompt.onNext(true);
    }
    @Override public void notifyMobileOfFollower(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyMobileOfFollower(b).build());
    }
    @Override public void notifyMobileOfFriendActivity(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyMobileOfFriendActivity(b).build());
    }
    @Override public void notifyMobileOfMessages(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyMobileOfMessages(b).build());
    }
    @Override public void notifyMobileOfUpdates(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyMobileOfUpdates(b).build());
    }
    @Override public void notifyOfFollower(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyOfFollower(b).build());
    }
    @Override public void notifyOfFriendActivity(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyOfFriendActivity(b).build());
    }
    @Override public void notifyOfMessages(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyOfMessages(b).build());
    }
    @Override public void notifyOfUpdates(final boolean b) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().notifyOfUpdates(b).build());
    }
    @Override public void optIntoFollowing(final boolean checked) {
      this.optIntoFollowing.onNext(checked);
    }
    @Override public void optOutOfFollowing(final boolean optOut) {
      this.optOutOfFollowing.onNext(optOut);
    }
    @Override public void sendGamesNewsletter(final boolean checked) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().gamesNewsletter(checked).build());
      this.newsletterInput.onNext(new Pair<>(checked, Newsletter.GAMES));
    }
    @Override public void sendHappeningNewsletter(final boolean checked) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().happeningNewsletter(checked).build());
      this.newsletterInput.onNext(new Pair<>(checked, Newsletter.HAPPENING));
    }
    @Override public void sendPromoNewsletter(final boolean checked) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().promoNewsletter(checked).build());
      this.newsletterInput.onNext(new Pair<>(checked, Newsletter.PROMO));
    }
    @Override public void sendWeeklyNewsletter(final boolean checked) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().weeklyNewsletter(checked).build());
      this.newsletterInput.onNext(new Pair<>(checked, Newsletter.WEEKLY));
    }
    @Override public void showPublicProfile(final boolean checked) {
      this.userInput.onNext(this.userOutput.getValue().toBuilder().showPublicProfile(checked).build());
    }

    @Override public @NonNull Observable<Void> logout() {
      return this.logout;
    }
    @Override public @NonNull Observable<Void> hideConfirmFollowingOptOutPrompt() {
      return this.hideConfirmFollowingOptOutPrompt;
    }
    @Override public @NonNull Observable<Boolean> showConfirmLogoutPrompt() {
      return this.showConfirmLogoutPrompt;
    }
    @Override public @NonNull Observable<Void> showConfirmFollowingOptOutPrompt() {
      return this.showConfirmFollowingOptOutPrompt;
    }
    @Override public @NonNull Observable<Void> showFollowingInfo() {
      return this.showFollowingInfo;
    }
    @Override public @NonNull Observable<Newsletter> showOptInPrompt() {
      return this.showOptInPrompt;
    }
    @Override public @NonNull Observable<Void> showRecommendationsInfo() {
      return this.showRecommendationsInfo;
    }
    @Override public @NonNull Observable<Void> showPrivateProfileInfo() { return  this.showPrivateProfileInfo; }
    @Override public @NonNull Observable<User> user() {
      return this.userOutput;
    }

    @Override public @NonNull Observable<String> unableToSavePreferenceError() {
      return this.unableToSavePreferenceError
        .takeUntil(this.updateSuccess)
        .map(__ -> null);
    }
  }
}
