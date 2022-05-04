package com.kickstarter.mock.factories

import com.kickstarter.models.pushdata.GCM.Companion.builder
import com.kickstarter.services.apiresponses.PushNotificationEnvelope

object PushNotificationEnvelopeFactory {
    fun envelope(): PushNotificationEnvelope {
        val gcm = builder()
            .alert("You've received a new push notification")
            .title("Hello")
            .build()
        return PushNotificationEnvelope.builder()
            .gcm(gcm)
            .build()
    }
}
