package utils
import android.app.Activity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText

class Matchers {

    fun <T : Activity> nextOpenActivityIs(activity: Class<T>) {
        intended(IntentMatchers.hasComponent(activity.name))
    }

    fun textMatches(textViewId: Int, textToMatch: String) {
        onView(withId(textViewId)).check(matches(withText(textToMatch)))
    }
}
