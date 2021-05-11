package com.kickstarter.viewmodels;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.libs.utils.UrlUtils;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.KSUri;
import com.kickstarter.ui.activities.DeepLinkActivity;

import androidx.annotation.NonNull;

import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;
import static com.kickstarter.libs.rx.transformers.Transformers.values;

public interface DeepLinkViewModel {

  interface Outputs {
    /** Emits when we should start an external browser because we don't want to deep link. */
    Observable<String> startBrowser();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.DiscoveryActivity}. */
    Observable<Void> startDiscoveryActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}. */
    Observable<Uri> startProjectActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity} with pledge sheet expanded. */
    Observable<Uri> startProjectActivityForCheckout();

    /** Emits when we should finish the current activity */
    Observable<Void> finisDeeplinkActivity();
  }

  final class ViewModel extends ActivityViewModel<DeepLinkActivity> implements Outputs {
    public ViewModel(final @NonNull Environment environment) {
      super(environment);
      final ApiClientType apiClientType = environment.apiClient();
      final CurrentUserType currentUser = environment.currentUser();

      final Observable<Uri> uriFromIntent = intent()
        .map(Intent::getData)
        .ofType(Uri.class);

      uriFromIntent
        .filter(this::lastPathSegmentIsProjects)
        .compose(ignoreValues())
        .compose(bindToLifecycle())
        .subscribe(this.startDiscoveryActivity::onNext);

      uriFromIntent
        .filter(uri -> KSUri.isProjectUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .filter(uri -> !KSUri.isCheckoutUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .filter(uri -> !KSUri.isProjectPreviewUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .map(this::appendRefTagIfNone)
        .compose(bindToLifecycle())
        .subscribe(this.startProjectActivity::onNext);

      uriFromIntent
        .map(KSUri::isSettingsUrl)
        .filter(Boolean::booleanValue)
        .compose(bindToLifecycle())
        .subscribe(this.updateUserPreferences::onNext);

      currentUser.observable()
        .filter(user -> !user.notifyMobileOfMarketingUpdate())
        .compose(combineLatestPair(this.updateUserPreferences))
        .distinctUntilChanged()
        .switchMap(it -> updateSettings(it.first, apiClientType))
        .compose(values())
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(user -> refreshUserAndFinishActivity(user, currentUser));

      uriFromIntent
        .filter(uri -> KSUri.isCheckoutUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .map(this::appendRefTagIfNone)
        .compose(bindToLifecycle())
        .subscribe(this.startProjectActivityWithCheckout::onNext);

      final Observable<Uri> projectPreview = uriFromIntent
        .filter(uri -> KSUri.isProjectPreviewUri(uri, Secrets.WebEndpoint.PRODUCTION));

      final Observable<Uri> unsupportedDeepLink = uriFromIntent
        .filter(uri -> !lastPathSegmentIsProjects(uri))
        .filter(uri -> !KSUri.isSettingsUrl(uri))
        .filter(uri -> !KSUri.isCheckoutUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .filter(uri -> !KSUri.isProjectUri(uri, Secrets.WebEndpoint.PRODUCTION));

      Observable.merge(projectPreview, unsupportedDeepLink)
        .map(Uri::toString)
        .filter(url -> !TextUtils.isEmpty(url))
        .compose(bindToLifecycle())
        .subscribe(this.startBrowser::onNext);
    }

    private void refreshUserAndFinishActivity(final User user, final CurrentUserType currentUser) {
      currentUser.refresh(user);
      this.finishDeeplinkActivity.onNext(null);
    }

    private Uri appendRefTagIfNone(final @NonNull Uri uri) {
      final String url = uri.toString();
      final String ref = UrlUtils.INSTANCE.refTag(url);
      if (ObjectUtils.isNull(ref)) {
        return Uri.parse(UrlUtils.INSTANCE.appendRefTag(url, RefTag.deepLink().tag()));
      }

      return uri;
    }

    private boolean lastPathSegmentIsProjects(final @NonNull Uri uri) {
      return uri.getLastPathSegment().equals("projects");
    }

    private Observable<Notification<User>> updateSettings(final User user, final ApiClientType apiClientType) {
      final User updatedUser = user.toBuilder().notifyMobileOfMarketingUpdate(true).build();
      return apiClientType.updateUserSettings(updatedUser)
              .materialize()
              .share();
    }

    private final BehaviorSubject<String> startBrowser = BehaviorSubject.create();
    private final BehaviorSubject<Void> startDiscoveryActivity = BehaviorSubject.create();
    private final BehaviorSubject<Uri> startProjectActivity = BehaviorSubject.create();
    private final BehaviorSubject<Uri> startProjectActivityWithCheckout = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> updateUserPreferences = BehaviorSubject.create();
    private final BehaviorSubject<Void> finishDeeplinkActivity = BehaviorSubject.create();
    public final Outputs outputs = this;

    @Override public @NonNull Observable<String> startBrowser() {
      return this.startBrowser;
    }
    @Override public @NonNull Observable<Void> startDiscoveryActivity() {
      return this.startDiscoveryActivity;
    }
    @Override public @NonNull Observable<Uri> startProjectActivity() {
      return this.startProjectActivity;
    }
    @Override public @NonNull Observable<Uri> startProjectActivityForCheckout() {
      return this.startProjectActivityWithCheckout;
    }
    @Override
    public Observable<Void> finisDeeplinkActivity() {
      return this.finishDeeplinkActivity;
    }
  }
}
