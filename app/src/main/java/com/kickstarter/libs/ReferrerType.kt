package com.kickstarter.libs

/*
 * describes the source of the referrer
 */
enum class ReferrerType(val referrerType: String) {
  CAMPAIGN("campaign"),
  DOMAIN("domain"),
  CUSTOM("custom"),
  EXTERNAL("external"),
  INTERNAL("internal")
}
