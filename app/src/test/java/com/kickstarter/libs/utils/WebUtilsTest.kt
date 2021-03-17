package com.kickstarter.libs.utils

import com.kickstarter.BuildConfig
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Build
import org.junit.Test
import java.util.Locale

class WebUtilsTest : KSRobolectricTestCase() {

    @Test
    fun testUserAgent() {
        val packageManager = context().packageManager

        val packageInfo = packageManager.getPackageInfo(context().applicationContext.packageName, 0)
        val variant = StringBuilder().append(BuildConfig.FLAVOR)
            .append(BuildConfig.BUILD_TYPE.substring(0, 1).toUpperCase(Locale.US))
            .append(BuildConfig.BUILD_TYPE.substring(1))
            .toString()
        val versionCode = packageInfo.versionCode
        val versionName = packageInfo.versionName
        assertEquals(
            "Kickstarter Android Mobile Variant/$variant Code/$versionCode Version/$versionName",
            WebUtils.userAgent(Build(packageInfo))
        )
    }
}
