package com.kickstarter.libs.models

class OptimizelyExperiment {
    enum class Key(val key: String) {
        PLEDGE_CTA_COPY("pledge_cta_copy")
    }

    enum class Variant(val rawValue: String?) {
        CONTROL(null),
        VARIANT_1("variant-1"),
        VARIANT_2("variant-2");

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
