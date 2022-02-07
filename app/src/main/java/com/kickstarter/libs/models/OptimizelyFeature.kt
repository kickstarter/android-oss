package com.kickstarter.libs.models

class OptimizelyFeature {
    enum class Key(val key: String) {
        LIGHTS_ON("android_lights_on"),
        PROJECT_PAGE_V2("android_project_page_v2"),
        ANDROID_STORY_TAB("android_story_tab")
    }
}
