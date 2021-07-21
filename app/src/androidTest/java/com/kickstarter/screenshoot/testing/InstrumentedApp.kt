package com.kickstarter.screenshoot.testing

import com.kickstarter.ApplicationComponent
import com.kickstarter.DaggerApplicationComponent
import com.kickstarter.KSApplication
import com.kickstarter.screenshoot.testing.di.AndroidTestApplicationModule

class InstrumentedApp : KSApplication() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun getComponent(): ApplicationComponent {
        return DaggerApplicationComponent.builder()
            .applicationModule(AndroidTestApplicationModule(this))
            .build()
    }

    override fun isInUnitTests(): Boolean {
        return true
    }
}
