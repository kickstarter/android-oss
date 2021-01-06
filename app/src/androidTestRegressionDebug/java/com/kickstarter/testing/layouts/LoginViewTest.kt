package com.kickstarter.testing.infrastructure.suite

import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import com.google.firebase.iid.FirebaseInstanceId
import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.ui.activities.DiscoveryActivity
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.net.CookieStore
import java.net.URI
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.test.platform.app.InstrumentationRegistry
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ViewHelpers
import com.kickstarter.R
import androidx.test.annotation.UiThreadTest

class LoginViewTest {
    @Test
    @UiThreadTest
    fun testSimpleScreenshot() {
        val unthemedContext = InstrumentationRegistry.getInstrumentation().getTargetContext()
        val context = ContextThemeWrapper(unthemedContext, R.style.KSTheme)


        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.signup_layout, null, false)

        ViewHelpers.setupView(view)
            .setExactWidthDp(400)
            .layout()

        Screenshot.snap(view).record()
    }
}
