package com.kickstarter.viewmodels.outputs;

import android.support.annotation.NonNull;

import com.kickstarter.services.DiscoveryParams;

import java.util.List;

import rx.Observable;

public interface DiscoveryViewModelOutputs {
  @NonNull Observable<List<DiscoveryParams>> filterParams();
}
