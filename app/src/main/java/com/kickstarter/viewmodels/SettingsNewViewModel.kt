package com.kickstarter.viewmodels

import android.content.Context
import android.content.Intent
import android.databinding.BaseObservable
import com.kickstarter.BuildConfig
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.ui.activities.HelpNewActivity
import com.kickstarter.ui.activities.NewsletterActivity
import com.kickstarter.ui.activities.NotificationsActivity
import com.kickstarter.ui.activities.PrivacyActivity

class SettingsNewViewModel(private val context: Context, private val environment: Environment) : BaseObservable() {

    fun getVersion() = BuildConfig.VERSION_NAME

    fun rateUsClick() = ViewUtils.openStoreRating(context, context.packageName)

    fun logoutClick() {
        environment.logout().execute()
        ApplicationUtils.startNewDiscoveryActivity(context)
    }

    fun notificationsClick() = context.startActivity(Intent(context, NotificationsActivity::class.java))

    fun newsletterClick() = context.startActivity(Intent(context, NewsletterActivity::class.java))

    fun helpClick() = context.startActivity(Intent(context, HelpNewActivity::class.java))

    fun privacyClick() = context.startActivity(Intent(context, PrivacyActivity::class.java))
}