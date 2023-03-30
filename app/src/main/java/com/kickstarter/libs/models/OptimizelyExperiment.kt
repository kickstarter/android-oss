package com.kickstarter.libs.models

class OptimizelyExperiment {
    enum class Key(val key: String)

    enum class Variant(val rawValue: String?) {
        CONTROL(null),
        VARIANT_1("variant_1"),
        VARIANT_2("variation_2"),
        VARIANT_3("variation_3"),
        VARIANT_4("variation_4");

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
