package utils
import android.app.Activity
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers

class Matchers {

    fun <T : Activity> nextOpenActivityIs(activity: Class<T>) {
        intended(IntentMatchers.hasComponent(activity.name))
    }
}