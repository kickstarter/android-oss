package com.kickstarter.viewmodels.outputs;

import com.kickstarter.services.apiresponses.InternalBuildEnvelope;

import rx.Observable;

public interface DownloadBetaViewModelOutputs {
  Observable<InternalBuildEnvelope> internalBuildEnvelope();
}
