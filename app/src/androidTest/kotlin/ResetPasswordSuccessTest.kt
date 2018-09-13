
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import com.kickstarter.R
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import utils.Events
import utils.Matchers


@RunWith(AndroidJUnit4::class)
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
