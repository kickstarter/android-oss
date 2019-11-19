package com.kickstarter.testing.login.suite
import android.content.Intent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.R
import com.kickstarter.testing.utils.Events
import com.kickstarter.testing.utils.Matchers
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.LoginActivity
import com.kickstarter.ui.data.LoginReason
import org.junit.Rule
import org.junit.Test

@SmallTest
class ResetPasswordSuccessTest {

    private val events = Events()
    private val checkThat = Matchers()

    @Rule
    @JvmField
    val activityRule: IntentsTestRule<LoginActivity> = object : IntentsTestRule<LoginActivity>(LoginActivity::class.java){
        override fun getActivityIntent(): Intent {
            val targetContext = InstrumentationRegistry.getInstrumentation()
                    .targetContext
            return Intent(targetContext, LoginActivity::class.java)
                    .putExtra(IntentKey.EMAIL, "email@test.com")
                    .putExtra(IntentKey.LOGIN_REASON, LoginReason.RESET_PASSWORD)
        }
    }

    @Test
    fun testShowingResetPasswordSuccessDialogAndPrefillingEmail() {
        checkThat.textMatches(R.id.message_text_view,"We’ve sent an email to email@test.com with instructions to reset your password.")
        events.clickOnView(R.id.ok_button)
        checkThat.textMatches(R.id.email,"email@test.com")
    }
}
