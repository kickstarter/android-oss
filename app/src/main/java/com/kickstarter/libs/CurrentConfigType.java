package com.kickstarter.libs;

import rx.Observable;

public interface CurrentConfigType {

  /**
   * Returns the config as an observable.
   */
  Observable<Config> observable();

  /**
   * Set a new config.
   */
  void config(Config config);
}
