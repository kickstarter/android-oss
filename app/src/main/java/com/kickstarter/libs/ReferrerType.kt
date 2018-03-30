package com.kickstarter.libs

import com.kickstarter.R

/*
 * describes the source of the referrer
 */
enum class ReferrerType(val referrerType: String, val referrerColorId : Int) {
  CUSTOM("custom", R.color.ksr_highlighter_green),
  EXTERNAL("external", R.color.ksr_green_500),
  KICKSTARTER("kickstarter", R.color.ksr_green_800)
}

