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

    fun helpClick() = context.startActivity(Intent(context, HelpNewActivity::class.java))

    fun logoutClick() {
        this.environment.logout().execute()
        this.environment.koala().trackLogout()
        ApplicationUtils.startNewDiscoveryActivity(context)
    }

    fun newsletterClick() = context.startActivity(Intent(context, NewsletterActivity::class.java))

    fun notificationsClick() = context.startActivity(Intent(context, NotificationsActivity::class.java))

    fun privacyClick() = context.startActivity(Intent(context, PrivacyActivity::class.java))

    fun rateUsClick() = ViewUtils.openStoreRating(context, context.packageName)

}