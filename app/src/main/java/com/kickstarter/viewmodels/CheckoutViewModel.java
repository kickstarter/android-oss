package com.kickstarter.viewmodels;

import android.util.Pair;

import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.gson.Gson;
import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.AndroidPayCapability;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.models.AndroidPayPayload;
import com.kickstarter.libs.utils.AndroidPayUtils;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.CheckoutActivity;
import com.kickstarter.ui.data.ActivityResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface CheckoutViewModel {

  interface Inputs {
    /** Call when any back button is pressed. */
    void backButtonClicked();

    /** Call when the user has clicked the confirm android pay button. */
    void confirmAndroidPayClicked();

    /** Takes a url whenever a page has been intercepted by the web view.
     * @param url The url that has been intercepted */
    void pageIntercepted(final @NonNull String url);

    /** Call when the activity obtains a base 64 payload from an android pay button in the web page. */
    void takePayloadString(final @Nullable String payloadString);
  }

  interface Outputs {
    /** Emits when we should show an Android Pay error. */
    Observable<Integer> androidPayError();

    /** Emits the masked wallet and android pay payload when it is time to attempt to convert the masked
     * wallet into a full wallet. */
    Observable<Pair<MaskedWallet, AndroidPayPayload>> attemptAndroidPayConfirmation();

    /** Emits a full wallet when android pay has been completely confirmed, and we are now ready to interact
     * with our payment processor. */
    Observable<FullWallet> completeAndroidPay();

    /** Emits a boolean that determines if the android pay confirmation should be shown or not. */
    Observable<Boolean> displayAndroidPayConfirmation();

    /** Emits a boolean if this device is capable of android pay. */
    Observable<Boolean> isAndroidPayAvailable();

    /** Emits when the activity should pop itself off the navigation stack. */
    Observable<Void> popActivityOffStack();

    /** The project associated with the current checkout. */
    Observable<Project> project();

    /** Emits a payload whenever an android pay sheet should be displayed.
     * Can emit `null`, which means a prompt should not be displayed. */
    Observable<AndroidPayPayload> showAndroidPaySheet();

    /** The title to display to the user. */
    Observable<String> title();

    /** Emits a masked wallet and payload when the confirmation view should be updated with the newest data. */
    Observable<Pair<MaskedWallet, AndroidPayPayload>> updateAndroidPayConfirmation();

    /** The URL the web view should load, if its state has been destroyed. */
    Observable<String> url();
  }

  final class ViewModel extends ActivityViewModel<CheckoutActivity> implements Inputs, Outputs {
    private AndroidPayCapability androidPayCapability;
    private Gson gson;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.androidPayCapability = environment.androidPayCapability();
      this.gson = environment.gson();

      intent()
        .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
        .ofType(Project.class)
        .compose(bindToLifecycle())
        .subscribe(this.project::onNext);

      intent()
        .map(i -> i.getStringExtra(IntentKey.TOOLBAR_TITLE))
        .ofType(String.class)
        .compose(bindToLifecycle())
        .subscribe(this.title::onNext);

      intent()
        .map(i -> i.getStringExtra(IntentKey.URL))
        .ofType(String.class)
        .take(1)
        .mergeWith(this.pageIntercepted)
        .compose(bindToLifecycle())
        .subscribe(this.url::onNext);

      final Observable<MaskedWallet> maskedWallet = activityResult()
        .filter(AndroidPayUtils::isMaskedWalletRequest)
        .map(ActivityResult::intent)
        .map(i -> i.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET))
        .ofType(MaskedWallet.class);

      final Observable<FullWallet> fullWallet = activityResult()
        .filter(AndroidPayUtils::isFullWalletRequest)
        .map(ActivityResult::intent)
        .map(i -> i.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET))
        .ofType(FullWallet.class);

      this.androidPayError = activityResult()
        .map(AndroidPayUtils::walletRequestError)
        .filter(ObjectUtils::isNotNull);

      final Observable<AndroidPayPayload> payload = this.payloadString
        .map(str -> AndroidPayUtils.payloadFromString(str, this.gson))
        .ofType(AndroidPayPayload.class);

      final Observable<Boolean> confirmationVisibilityOnBack = this.displayAndroidPayConfirmation
        .compose(takeWhen(this.backButtonClicked));

      payload
        .compose(bindToLifecycle())
        .subscribe(this.showAndroidPaySheet::onNext);

      confirmationVisibilityOnBack
        .filter(BooleanUtils::isFalse)
        .compose(bindToLifecycle())
        .subscribe(__ -> this.popActivityOffStack.onNext(null));

      confirmationVisibilityOnBack
        .filter(BooleanUtils::isTrue)
        .map(__ -> false)
        .mergeWith(maskedWallet.map(__ -> true))
        .compose(bindToLifecycle())
        .subscribe(this.displayAndroidPayConfirmation::onNext);

      maskedWallet
        .compose(combineLatestPair(payload))
        .filter(wp -> wp.first != null && wp.second != null)
        .compose(bindToLifecycle())
        .subscribe(this.updateAndroidPayConfirmation::onNext);

      maskedWallet
        .compose(combineLatestPair(payload))
        .compose(takeWhen(this.confirmAndroidPayClicked))
        .filter(wp -> wp.second != null)
        .compose(bindToLifecycle())
        .subscribe(this.attemptAndroidPayConfirmation::onNext);

      fullWallet
        .compose(bindToLifecycle())
        .subscribe(this.completeAndroidPay::onNext);

      this.showAndroidPaySheet
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackShowAndroidPaySheet());

      this.completeAndroidPay
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackAndroidPayFinished());

      // Start by not showing the confirmation page.
      this.displayAndroidPayConfirmation.onNext(false);
    }

    private final PublishSubject<Void> backButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> confirmAndroidPayClicked = PublishSubject.create();
    private final PublishSubject<String> pageIntercepted = PublishSubject.create();
    private final PublishSubject<String> payloadString = PublishSubject.create();

    private final Observable<Integer> androidPayError;
    private final BehaviorSubject<Pair<MaskedWallet, AndroidPayPayload>> attemptAndroidPayConfirmation = BehaviorSubject.create();
    private final BehaviorSubject<FullWallet> completeAndroidPay = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> displayAndroidPayConfirmation = BehaviorSubject.create();
    private final BehaviorSubject<Void> popActivityOffStack = BehaviorSubject.create();
    private final BehaviorSubject<Project> project = BehaviorSubject.create();
    private final BehaviorSubject<AndroidPayPayload> showAndroidPaySheet = BehaviorSubject.create();
    private final BehaviorSubject<String> title = BehaviorSubject.create();
    private final BehaviorSubject<Pair<MaskedWallet, AndroidPayPayload>> updateAndroidPayConfirmation = BehaviorSubject.create();
    private final BehaviorSubject<String> url = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void pageIntercepted(final @NonNull String str) {
      this.pageIntercepted.onNext(str);
    }
    @Override public void backButtonClicked() {
      this.backButtonClicked.onNext(null);
    }
    @Override public void takePayloadString(final @Nullable String payloadString) {
      this.payloadString.onNext(payloadString);
    }
    @Override public void confirmAndroidPayClicked() {
      this.confirmAndroidPayClicked.onNext(null);
    }

    @Override public @NonNull Observable<Integer> androidPayError() {
      return this.androidPayError;
    }
    @Override public @NonNull Observable<Pair<MaskedWallet, AndroidPayPayload>> attemptAndroidPayConfirmation() {
      return this.attemptAndroidPayConfirmation;
    }
    @Override public @NonNull Observable<FullWallet> completeAndroidPay() {
      return this.completeAndroidPay;
    }
    @Override public @NonNull Observable<Boolean> displayAndroidPayConfirmation() {
      return this.displayAndroidPayConfirmation;
    }
    @Override public @NonNull Observable<Boolean> isAndroidPayAvailable() {
      return Observable.just(this.androidPayCapability.isCapable());
    }
    @Override public @NonNull Observable<Void> popActivityOffStack() {
      return this.popActivityOffStack;
    }
    @Override public @NonNull Observable<Project> project() {
      return this.project;
    }
    @Override public @NonNull Observable<AndroidPayPayload> showAndroidPaySheet() {
      return this.showAndroidPaySheet;
    }
    @Override public @NonNull Observable<String> title() {
      return this.title;
    }
    @Override public @NonNull Observable<String> url() {
      return this.url;
    }
    @Override public @NonNull Observable<Pair<MaskedWallet, AndroidPayPayload>> updateAndroidPayConfirmation() {
      return this.updateAndroidPayConfirmation;
    }
  }
}
