package com.kickstarter.libs.models

class OptimizelyFeature {
    enum class Key(val key: String) {
        LIGHTS_ON("android_lights_on"),
        ANDROID_LOCAL_PICKUP("android_local_pickup"),
        ANDROID_PAYMENTSHEET("android_paymentsheet"),
        ANDROID_FACEBOOK_LOGIN_REMOVE("android_facebook_login_remove"),
        ANDROID_PAYMENTSHEET_SETTINGS("android_paymentsheet_user_settings"),
        ANDROID_HIDE_APP_RATING_DIALOG("android_hide_app_rating_dialog")
    }
}
