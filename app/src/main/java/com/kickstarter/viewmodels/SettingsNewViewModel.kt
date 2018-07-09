package com.kickstarter.viewmodels

import android.content.Context
import android.databinding.BaseObservable
import com.kickstarter.BuildConfig
import com.kickstarter.libs.Environment
import com.kickstarter.libs.Logout
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ViewUtils

class SettingsNewViewModel(private val context: Context, private val environment: Environment) : BaseObservable() {

    fun getVersion() = BuildConfig.VERSION_NAME

    fun rateUsClick() {
        ViewUtils.openStoreRating(context, context.packageName)
    }

    fun logoutClick() {
        environment.logout().execute()
        ApplicationUtils.startNewDiscoveryActivity(context)
    }

    fun notificationsClick() {

    }

    fun newsletterClick() {

    }

    fun helpClick() {

    }

    fun privacyClick() {

    }
}