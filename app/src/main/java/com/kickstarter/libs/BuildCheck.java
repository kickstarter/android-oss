package com.kickstarter.libs;

import com.kickstarter.presenters.DiscoveryPresenter;
import com.kickstarter.services.KickstarterClient;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public interface BuildCheck {
  void bind(final DiscoveryPresenter presenter, final KickstarterClient client);

  BuildCheck DEFAULT = (presenter, client) -> {
    final Subscription subscription = RxUtils.combineLatestPair(client.pingBeta(), presenter.viewSubject())
      .filter(v -> v.second != null)
      .filter(v -> v.first.newerBuildAvailable())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(v -> v.second.showBuildAlert()); // TODO: Pass build info

    presenter.addSubscription(subscription);
  };
}
