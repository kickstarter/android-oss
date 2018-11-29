package com.kickstarter.extensions

import android.content.Context
import android.os.Bundle
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.google.firebase.analytics.FirebaseAnalytics

fun fabricLogCustomEvent(customEvent: String) {
    Answers.getInstance().logCustom(CustomEvent(customEvent))
}

fun fabricLogCustomEventWithAttributes(customEvent: String, key: String, attribute: String) {
    Answers.getInstance().logCustom(CustomEvent(customEvent).putCustomAttribute(key, attribute))
}
