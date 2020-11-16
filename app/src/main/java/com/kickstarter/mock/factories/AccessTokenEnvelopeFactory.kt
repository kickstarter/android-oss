package com.kickstarter.mock.factories

import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.AccessTokenEnvelope

class AccessTokenEnvelopeFactory private constructor() {
    companion object {
        fun envelope(user: User, token: String): AccessTokenEnvelope {
            return AccessTokenEnvelope.builder()
                    .user(user)
                    .accessToken(token)
                    .build()
        }

        fun envelope(): AccessTokenEnvelope {
            return envelope(UserFactory.userNotVerifiedEmail(), "Token")
        }
    }
}