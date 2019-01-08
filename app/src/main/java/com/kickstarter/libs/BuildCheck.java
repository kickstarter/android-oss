package com.kickstarter.libs;

import com.kickstarter.services.WebClientType;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.viewmodels.DiscoveryViewModel;

import androidx.annotation.NonNull;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public interface BuildCheck {
  void bind(final @NonNull DiscoveryViewModel.ViewModel viewModel, final @NonNull WebClientType client);

  BuildCheck DEFAULT = (viewModel, client) -> client.pingBeta()
    .filter(InternalBuildEnvelope::newerBuildAvailable)
    .compose(viewModel.bindToLifecycle())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(viewModel.inputs::newerBuildIsAvailable, e -> Timber.e(e.toString()));
}
