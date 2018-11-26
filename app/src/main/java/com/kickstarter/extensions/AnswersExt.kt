package com.kickstarter.extensions

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent

fun fabricLogCustomEvent(customEvent: String) {
    Answers.getInstance().logCustom(CustomEvent(customEvent))
}
