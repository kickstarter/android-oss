package com.kickstarter.viewmodels.outputs;

import com.kickstarter.services.apiresponses.InternalBuildEnvelope;

import rx.Observable;

public interface DownloadBetaViewModelOutputs {
  /**
   * Returns the latest internal build data.
   */
  Observable<InternalBuildEnvelope> internalBuildEnvelope();
}
