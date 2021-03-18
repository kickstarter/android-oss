package com.kickstarter

import android.content.Context
import androidx.multidex.MultiDex
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(MultiDex::class)
object ShadowAndroidXMultiDex {

    @JvmStatic
    @Implementation
    fun install(context: Context) {}
}
