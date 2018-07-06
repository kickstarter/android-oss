package com.kickstarter.viewmodels

import android.content.Context
import android.databinding.BaseObservable
import com.kickstarter.BuildConfig
import com.kickstarter.libs.utils.ViewUtils

class SettingsNewViewModel(private val context: Context) : BaseObservable() {

    fun getVersion() = BuildConfig.VERSION_NAME

    fun rateUsClick() {
        ViewUtils.openStoreRating(context, context.packageName)
    }

}