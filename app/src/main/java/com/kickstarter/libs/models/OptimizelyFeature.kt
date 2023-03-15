package com.kickstarter.libs.models

class OptimizelyFeature {
    enum class Key(val key: String) {
        LIGHTS_ON("android_lights_on"),
        ANDROID_PAYMENTSHEET("android_paymentsheet"),
        ANDROID_FACEBOOK_LOGIN_REMOVE("android_facebook_login_remove"),
        ANDROID_PAYMENTSHEET_SETTINGS("android_paymentsheet_user_settings"),
        ANDROID_HIDE_APP_RATING_DIALOG("android_hide_app_rating_dialog"),
        ANDROID_CONSENT_MANAGEMENT("android_consent_management"),
        ANDROID_CAPI_INTEGRATION("android_capi_integration"),
        ANDROID_GOOGLE_ANALYTICS("android_google_analytics"),
        ANDROID_PRE_LAUNCH_SCREEN("android_pre_launch_screen"),
        ANDROID_UGC("android_ugc")
    }
}
