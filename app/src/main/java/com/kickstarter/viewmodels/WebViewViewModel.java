package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.WebViewActivity;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public interface WebViewViewModel {

  interface Outputs {
    /** Emits a string to display in the toolbar.*/
    Observable<String> toolbarTitle();

    /** Emits a URL to load in the web view. */
    Observable<String> url();
  }

  final class ViewModel extends ActivityViewModel<WebViewActivity> implements Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      intent()
        .map(i -> i.getStringExtra(IntentKey.TOOLBAR_TITLE))
        .ofType(String.class)
        .compose(bindToLifecycle())
        .subscribe(this.toolbarTitle::onNext);

      intent()
        .map(i -> i.getStringExtra(IntentKey.URL))
        .ofType(String.class)
        .compose(bindToLifecycle())
        .subscribe(this.url::onNext);

      intent()
        .map(i -> i.getParcelableExtra(IntentKey.PUSH_NOTIFICATION_ENVELOPE))
        .ofType(PushNotificationEnvelope.class)
        .filter(ObjectUtils::isNotNull)
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackPushNotification);
    }

    private final BehaviorSubject<String> toolbarTitle = BehaviorSubject.create();
    private final BehaviorSubject<String> url = BehaviorSubject.create();

    public final Outputs outputs = this;

    @Override public @NonNull Observable<String> toolbarTitle() {
      return this.toolbarTitle;
    }
    @Override public @NonNull Observable<String> url() {
      return this.url;
    }
  }
}
