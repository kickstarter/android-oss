package com.kickstarter.kickstarter.testing.settings.suite

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(SettingsActivityTest::class,
        PrivacyActivityTest::class,
        NotificationsActivityTest::class,
        HelpSettingsActivityTest::class)
class SettingsSuite
