package com.kickstarter.libs;

import android.support.annotation.NonNull;

import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.viewmodels.DiscoveryViewModel;
import com.kickstarter.services.WebClient;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public interface BuildCheck {
  void bind(@NonNull final DiscoveryViewModel viewModel, @NonNull final WebClient client);

  BuildCheck DEFAULT = (viewModel, client) -> {
    final Subscription subscription = client.pingBeta()
      .compose(Transformers.combineLatestPair(viewModel.viewSubject()))
      .filter(v -> v.first.newerBuildAvailable())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(v -> v.second.showBuildAlert(v.first), e -> Timber.e(e.toString()));

    viewModel.addSubscription(subscription);
  };
}
