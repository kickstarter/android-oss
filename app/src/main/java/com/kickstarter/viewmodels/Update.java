package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.UpdateActivity;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public interface Update {

  interface Inputs {

  }

  interface Outputs {
    /** Emits an update to start the comments activity with. */
    Observable<com.kickstarter.models.Update> startCommentsActivity();

    /** Emits a string to display in the toolbar title. */
    Observable<String> updateSequence();

    /** Emits a web view url to display. */
    Observable<String> webViewUrl();
  }

  final class ViewModel extends ActivityViewModel<UpdateActivity> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<com.kickstarter.models.Update> update = intent()
        .map(i -> i.getParcelableExtra(IntentKey.UPDATE))
        .ofType(com.kickstarter.models.Update.class)
        .filter(ObjectUtils::isNotNull);

      update
        .map(u -> u.urls().web().update())
        .subscribe(this.webViewUrl::onNext);

      update
        .map(u -> NumberUtils.format(u.sequence()))
        .subscribe(this.updateSequence::onNext);
    }

    private final PublishSubject<com.kickstarter.models.Update> startCommentsActivity = PublishSubject.create();
    private final BehaviorSubject<String> updateSequence = BehaviorSubject.create();
    private final BehaviorSubject<String> webViewUrl = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public @NonNull Observable<com.kickstarter.models.Update> startCommentsActivity() {
      return startCommentsActivity;
    }
    @Override public Observable<String> updateSequence() {
      return updateSequence;
    }
    @Override public @NonNull Observable<String> webViewUrl() {
      return webViewUrl;
    }
  }
}
