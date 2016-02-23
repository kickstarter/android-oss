package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.DownloadBetaActivity;
import com.kickstarter.viewmodels.inputs.DownloadBetaViewModelInputs;
import com.kickstarter.viewmodels.outputs.DownloadBetaViewModelOutputs;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class DownloadBetaViewModel extends ViewModel<DownloadBetaActivity> implements DownloadBetaViewModelInputs,
  DownloadBetaViewModelOutputs {

  private final BehaviorSubject<InternalBuildEnvelope> internalBuildEnvelope = BehaviorSubject.create();
  @Override
  public Observable<InternalBuildEnvelope> internalBuildEnvelope() {
    return internalBuildEnvelope;
  }

  public final DownloadBetaViewModelOutputs outputs = this;

  public DownloadBetaViewModel(final @NonNull Environment environment) {
    super(environment);
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    addSubscription(
      intent()
        .map(i -> i.getParcelableExtra(IntentKey.INTERNAL_BUILD_ENVELOPE))
        .ofType(InternalBuildEnvelope.class)
        .subscribe(internalBuildEnvelope)
    );
  }
}
