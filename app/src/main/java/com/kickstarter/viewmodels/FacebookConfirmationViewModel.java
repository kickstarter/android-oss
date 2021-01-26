package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.FacebookConfirmationActivity;

import androidx.annotation.NonNull;
import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.errors;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.values;

public interface FacebookConfirmationViewModel {

  interface Inputs {
    /** Call when the create new account button has been clicked. */
    void createNewAccountClick();

    /** Call when the send newsletter switch has been toggled. */
    void sendNewslettersClick(boolean __);
  }

  interface Outputs {
    /** Fill the view's email address. */
    Observable<String> prefillEmail();

    /** Emits a string to display when sign up fails. */
    Observable<String> signupError();

    /** Finish Facebook confirmation activity with OK result. */
    Observable<Void> signupSuccess();

    /** Emits a boolean to check send newsletter switch. */
    Observable<Boolean> sendNewslettersIsChecked();
  }

  final class ViewModel extends ActivityViewModel<FacebookConfirmationActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;
    private final CurrentConfigType currentConfig;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentConfig = environment.currentConfig();
      this.currentUser = environment.currentUser();

      final Observable<String> facebookAccessToken = intent()
        .map(i -> i.getStringExtra(IntentKey.FACEBOOK_TOKEN))
        .ofType(String.class);

      final Observable<Pair<String, Boolean>> tokenAndNewsletter = facebookAccessToken
        .compose(combineLatestPair(this.sendNewslettersIsChecked));

      intent()
        .map(i -> i.getParcelableExtra(IntentKey.FACEBOOK_USER))
        .ofType(ErrorEnvelope.FacebookUser.class)
        .map(ErrorEnvelope.FacebookUser::email)
        .compose(bindToLifecycle())
        .subscribe(this.prefillEmail::onNext);

      final Observable<Notification<AccessTokenEnvelope>> createNewAccountNotification = tokenAndNewsletter
        .compose(takeWhen(this.createNewAccountClick))
        .flatMap(tn -> this.client.registerWithFacebook(tn.first, tn.second))
        .share()
        .materialize();

      createNewAccountNotification
        .compose(errors())
        .map(ErrorEnvelope::fromThrowable)
        .map(ErrorEnvelope::errorMessage)
        .takeUntil(this.signupSuccess)
        .compose(bindToLifecycle())
        .subscribe(this.signupError);

      createNewAccountNotification
        .compose(values())
        .ofType(AccessTokenEnvelope.class)
        .compose(bindToLifecycle())
        .subscribe(this::registerWithFacebookSuccess);

      this.sendNewslettersClick
        .compose(bindToLifecycle())
        .subscribe(this.sendNewslettersIsChecked::onNext);

      this.currentConfig.observable()
        .take(1)
        .map(config -> false)
        .subscribe(this.sendNewslettersIsChecked::onNext);
    }

    private void registerWithFacebookSuccess(final @NonNull AccessTokenEnvelope envelope) {
      this.currentUser.login(envelope.user(), envelope.accessToken());
      this.signupSuccess.onNext(null);
    }

    private final PublishSubject<Void> createNewAccountClick = PublishSubject.create();
    private final PublishSubject<Boolean> sendNewslettersClick = PublishSubject.create();

    private final BehaviorSubject<String> prefillEmail = BehaviorSubject.create();
    private final PublishSubject<String> signupError = PublishSubject.create();
    private final PublishSubject<Void> signupSuccess = PublishSubject.create();
    private final BehaviorSubject<Boolean> sendNewslettersIsChecked = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void createNewAccountClick() {
      this.createNewAccountClick.onNext(null);
    }
    @Override public void sendNewslettersClick(final boolean b) {
      this.sendNewslettersClick.onNext(b);
    }

    @Override public @NonNull Observable<String> prefillEmail() {
      return this.prefillEmail;
    }
    @Override public @NonNull Observable<String> signupError() {
      return this.signupError;
    }
    @Override public @NonNull Observable<Void> signupSuccess() {
      return this.signupSuccess;
    }
    @Override public @NonNull Observable<Boolean> sendNewslettersIsChecked() {
      return this.sendNewslettersIsChecked;
    }
  }
}
