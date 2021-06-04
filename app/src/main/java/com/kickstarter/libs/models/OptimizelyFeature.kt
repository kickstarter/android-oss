package com.kickstarter.libs.models

class OptimizelyFeature {
    enum class Key(val key: String) {
        LIGHTS_ON("android_lights_on"),
        COMMENT_THREADING("android_comment_threading"),
        COMMENT_ENABLE_THREADS("android_comment_enable_threads"),
    }
}
