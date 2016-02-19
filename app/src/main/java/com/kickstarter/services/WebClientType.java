package com.kickstarter.services;

import com.kickstarter.services.apiresponses.InternalBuildEnvelope;

import rx.Observable;

public interface WebClientType {
  Observable<InternalBuildEnvelope> pingBeta();
}
