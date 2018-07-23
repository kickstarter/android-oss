package com.kickstarter.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.BaseObservable
import android.net.Uri
import android.text.TextUtils
import com.kickstarter.BuildConfig
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.models.User
import com.kickstarter.ui.activities.HelpActivity
import rx.android.schedulers.AndroidSchedulers
import java.util.*

class HelpNewViewModel(private val context: Context, private val environment: Environment) : BaseObservable() {

    fun contactClick() {
        this.environment.koala().trackContactEmailClicked()
        this.environment.currentUser().observable()
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::composeContactEmail)
    }

    fun cookiePolicyClick() {
        context.startActivity(Intent(context, HelpActivity.CookiePolicy::class.java))
        startActivityWithTransition()
    }

    fun helpCenterClick() {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Secrets.HelpCenter.ENDPOINT)))
        startActivityWithTransition()
    }

    fun howKSWorksClick() {
        context.startActivity(Intent(context, HelpActivity.HowItWorks::class.java))
        startActivityWithTransition()
    }

    fun privacyPolicyClick() {
        context.startActivity(Intent(context, HelpActivity.Privacy::class.java))
        startActivityWithTransition()
    }

    fun termsClick() {
        context.startActivity(Intent(context, HelpActivity.Terms::class.java))
        startActivityWithTransition()
    }

    private fun startActivityWithTransition() {
        val activity = context as Activity
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun composeContactEmail(user: User?) {
        val debugInfo = Arrays.asList(
                user?.id()?.toString() ?: context.getString((R.string.Logged_Out)),
                BuildConfig.VERSION_NAME,
                android.os.Build.VERSION.RELEASE + " (SDK " + Integer.toString(android.os.Build.VERSION.SDK_INT) + ")",
                android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
        )

        val body = StringBuilder()
                .append(context.getString(R.string.support_email_body))
                .append(TextUtils.join(" | ", debugInfo))
                .toString()

        val intent = Intent(Intent.ACTION_SENDTO)
                .setData(Uri.parse(context.getString(R.string.mailto)))
                .putExtra(Intent.EXTRA_SUBJECT, "[Android] " + context.getString(R.string.support_email_subject))
                .putExtra(Intent.EXTRA_TEXT, body)
                .putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(context.getString(R.string.support_email_to_android)))

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.support_email_chooser)))
        }
    }
}
