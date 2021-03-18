package com.kickstarter.libs

import com.kickstarter.R

/*
 * describes the source of the referrer
 */
enum class ReferrerType(val referrerType: String, val referrerColorId: Int) {
    CUSTOM("custom", R.color.kds_create_300),
    EXTERNAL("external", R.color.kds_create_700),
    KICKSTARTER("kickstarter", R.color.kds_create_700)
}
