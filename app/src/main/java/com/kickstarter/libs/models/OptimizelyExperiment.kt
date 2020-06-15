package com.kickstarter.libs.models

class OptimizelyExperiment {
    enum class Key(val key: String) {
        PLEDGE_CTA_COPY("pledge_cta_copy"),
        CAMPAIGN_DETAILS("native_project_page_campaign_details"),
        CREATOR_DETAILS("native_project_page_conversion_creator_details"),
        SUGGESTED_NO_REWARD_AMOUNT("suggested_no_reward_amount")
    }

    enum class Variant(val rawValue: String?) {
        CONTROL(null),
        VARIANT_1("variant-1"),
        VARIANT_2("variant-2"),
        VARIANT_3("variant-3"),
        VARIANT_4("variant-4");

        companion object {
            fun safeValueOf(rawValue: String?): Variant {
                for (enumValue in values()) {
                    if (enumValue.rawValue == rawValue) {
                        return enumValue
                    }
                }
                return CONTROL
            }
        }
    }
}
