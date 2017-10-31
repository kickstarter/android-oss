package com.kickstarter.libs

import android.graphics.Color

/*
 * describes the source of the referrer
 */
enum class ReferrerType(val referrerType: String) {
  CAMPAIGN("campaign"),
  DOMAIN("domain"),
  CUSTOM("custom"),
  EXTERNAL("external"),
  INTERNAL("kickstarter")
}

enum class ReferrerColor(val referrerColor : Int) {
  CAMPAIGN(Color.BLUE),
  DOMAIN(Color.GREEN),
  CUSTOM(Color.GRAY),
  EXTERNAL(Color.BLACK),
  INTERNAL(Color.DKGRAY)
}
