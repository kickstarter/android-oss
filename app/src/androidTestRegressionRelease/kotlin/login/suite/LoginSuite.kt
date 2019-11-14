package infrastructure.suite

import login.suite.ResetPasswordSuccessTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(ResetPasswordSuccessTest::class)
class LoginSuite
