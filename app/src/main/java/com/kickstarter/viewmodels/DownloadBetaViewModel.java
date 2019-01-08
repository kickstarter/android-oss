package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.DownloadBetaActivity;
import com.kickstarter.viewmodels.inputs.DownloadBetaViewModelInputs;
import com.kickstarter.viewmodels.outputs.DownloadBetaViewModelOutputs;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class DownloadBetaViewModel extends ActivityViewModel<DownloadBetaActivity> implements DownloadBetaViewModelInputs,
  DownloadBetaViewModelOutputs {

  private final BehaviorSubject<InternalBuildEnvelope> internalBuildEnvelope = BehaviorSubject.create();
  @Override
  public Observable<InternalBuildEnvelope> internalBuildEnvelope() {
    return this.internalBuildEnvelope;
  }

  public final DownloadBetaViewModelOutputs outputs = this;

  public DownloadBetaViewModel(final @NonNull Environment environment) {
    super(environment);

    intent()
      .map(i -> i.getParcelableExtra(IntentKey.INTERNAL_BUILD_ENVELOPE))
      .ofType(InternalBuildEnvelope.class)
      .compose(bindToLifecycle())
      .subscribe(this.internalBuildEnvelope);
  }
}
