package com.kickstarter.mock

import com.kickstarter.libs.Config
import com.kickstarter.libs.CurrentConfigTypeV2
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class MockCurrentConfigV2 : CurrentConfigTypeV2 {
    private val config = BehaviorSubject.create<Config>()
    override fun observable(): Observable<Config> {
        return config
    }

    override fun config(config: Config) {
        this.config.onNext(config)
    }
}
