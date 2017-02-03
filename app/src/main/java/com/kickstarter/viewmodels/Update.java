package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.UpdateActivity;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public interface Update {

  interface Inputs {

  }

  interface Outputs {
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
    }

    private final BehaviorSubject<String> webViewUrl = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public @NonNull Observable<String> webViewUrl() {
      return webViewUrl;
    }
  }
}
