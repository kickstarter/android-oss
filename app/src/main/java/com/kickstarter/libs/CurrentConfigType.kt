package com.kickstarter.libs

import io.reactivex.Observable


interface CurrentConfigType {
    /**
     * Returns the config as an observable.
     */
    fun observable(): Observable<Config>

    /**
     * Set a new config.
     */
    fun config(config: Config)
}
