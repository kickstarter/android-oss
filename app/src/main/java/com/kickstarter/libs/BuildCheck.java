package com.kickstarter.libs;

import android.support.annotation.NonNull;

import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.RxUtils;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.kickstarter.services.WebClient;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public interface BuildCheck {
  void bind(@NonNull final DiscoveryPresenter presenter, @NonNull final WebClient client);

  BuildCheck DEFAULT = (presenter, client) -> {
    final Subscription subscription = client.pingBeta()
      .compose(Transformers.combineLatestPair(presenter.viewSubject()))
      .filter(v -> v.first.newerBuildAvailable())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(v -> v.second.showBuildAlert(v.first), e -> Timber.e(e.toString()));

    presenter.addSubscription(subscription);
  };
}
