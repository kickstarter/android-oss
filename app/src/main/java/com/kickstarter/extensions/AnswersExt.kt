package com.kickstarter.extensions

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent

fun fabricLogCustomEvent(customEvent: String) {
    Answers.getInstance().logCustom(CustomEvent(customEvent))
}

fun fabricLogCustomEventWithAttributes(customEvent: String, key: String, attribute: String) {
    Answers.getInstance().logCustom(CustomEvent(customEvent).putCustomAttribute(key, attribute))
}
