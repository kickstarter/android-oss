package infrastructure.suite

import org.junit.runner.RunWith
import org.junit.runners.Suite
import settings.suite.HelpSettingsActivityTest
import settings.suite.NotificationsActivityTest
import settings.suite.PrivacyActivityTest
import settings.suite.SettingsActivityTest

@RunWith(Suite::class)
@Suite.SuiteClasses(SettingsActivityTest::class,
        PrivacyActivityTest::class,
        NotificationsActivityTest::class,
        HelpSettingsActivityTest::class)
class SettingsSuite
