package infrastructure.suite

import org.junit.runner.RunWith
import org.junit.runners.Suite
import settings.suite.NotificationsActivityTest
import settings.suite.SettingsActivityTest

@RunWith(Suite::class)
@Suite.SuiteClasses(SettingsActivityTest::class,
        NotificationsActivityTest::class,
        )
class SettingsSuite