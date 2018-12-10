package utils
import android.app.Activity
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText

class Matchers {

    fun <T : Activity> nextOpenActivityIs(activity: Class<T>) {
        intended(IntentMatchers.hasComponent(activity.name))
    }

    fun textMatches(textViewId: Int, textToMatch: String) {
        onView(withId(textViewId)).check(matches(withText(textToMatch)))
    }
}
