package com.kickstarter.screenshoot.testing

import com.kickstarter.AndroidTestApplicationModule
import com.kickstarter.ApplicationComponent
import com.kickstarter.DaggerApplicationComponent
import com.kickstarter.KSApplication

class InstrumentedApp : KSApplication() {
    override fun getComponent(): ApplicationComponent? {
        return DaggerApplicationComponent.builder()
            .applicationModule(AndroidTestApplicationModule(this))
            .build()
    }

    override fun isInUnitTests(): Boolean {
        return true
    }
}
