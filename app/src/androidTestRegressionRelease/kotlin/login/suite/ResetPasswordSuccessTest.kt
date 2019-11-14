package login.suite
import android.content.Intent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.R
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.LoginActivity
import org.junit.Rule
import org.junit.Test
import utils.Events
import utils.Matchers

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
            val result = Intent(targetContext, LoginActivity::class.java)
            result.putExtra(IntentKey.EMAIL, "email@test.com")
            return result
        }
    }

    @Test
    fun testShowingResetPasswordSuccessDialogAndPrefillingEmail() {
        checkThat.textMatches(R.id.message_text_view,"We’ve sent an email to email@test.com with instructions to reset your password.")
        events.clickOnView(R.id.ok_button)
        checkThat.textMatches(R.id.email,"email@test.com")
    }
}
