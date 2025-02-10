package com.kickstarter.screenshoot.testing

import com.kickstarter.ApplicationComponent
import com.kickstarter.DaggerApplicationComponent
import com.kickstarter.KSApplication
import com.kickstarter.screenshoot.testing.di.AndroidTestApplicationModule

class InstrumentedApp : KSApplication() {

    override val isInUnitTests: Boolean
        get() = true

    override fun getComponent(): ApplicationComponent {
        return DaggerApplicationComponent.builder()
            .applicationModule(AndroidTestApplicationModule(this))
            .build()
    }
}
