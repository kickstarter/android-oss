package com.kickstarter.libs;

import android.support.annotation.NonNull;

import com.kickstarter.presenters.DiscoveryPresenter;
import com.kickstarter.services.KickstarterClient;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public interface BuildCheck {
  void bind(@NonNull final DiscoveryPresenter presenter, @NonNull final KickstarterClient client);

  BuildCheck DEFAULT = (presenter, client) -> {
    final Subscription subscription = RxUtils.combineLatestPair(client.pingBeta(), presenter.viewSubject())
      .filter(v -> v.first.newerBuildAvailable())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(v -> v.second.showBuildAlert(v.first));

    presenter.addSubscription(subscription);
  };
}
