package com.kickstarter.ui.activities

import android.os.Bundle
import android.support.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.KSString
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.BooleanUtils.isTrue
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.User
import com.kickstarter.ui.data.Newsletter
import com.kickstarter.viewmodels.NewsletterViewModel
import kotlinx.android.synthetic.main.activity_newsletter.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(NewsletterViewModel.ViewModel::class)
class NewsletterActivity : BaseActivity<NewsletterViewModel.ViewModel>() {

    private lateinit var build: Build
    private lateinit var currentUserType: CurrentUserType
    private lateinit var ksString: KSString

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newsletter)

        this.build = environment().build()
        this.currentUserType = environment().currentUser()
        this.ksString = environment().ksString()

        this.viewModel.outputs.user()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::displayPreferences)

        this.viewModel.errors.unableToSavePreferenceError()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.showToast(this, getString(R.string.profile_settings_error)) }

        this.viewModel.outputs.showOptInPrompt()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showOptInPrompt)

        happening_switch.setOnClickListener { viewModel.inputs.sendHappeningNewsletter(happening_switch.isChecked) }
        news_events_switch.setOnClickListener { viewModel.inputs.sendPromoNewsletter(news_events_switch.isChecked) }
        projects_we_love_switch.setOnClickListener { viewModel.inputs.sendWeeklyNewsletter(projects_we_love_switch.isChecked) }
    }

    private fun displayPreferences(@NonNull user: User) {
        SwitchCompatUtils.setCheckedWithoutAnimation(happening_switch, isTrue(user.happeningNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(news_events_switch, isTrue(user.promoNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(projects_we_love_switch, isTrue(user.weeklyNewsletter()))
    }

    private fun newsletterString(@NonNull newsletter: Newsletter): String? {
        return when (newsletter) {
            Newsletter.HAPPENING -> getString(R.string.profile_settings_newsletter_happening)
            Newsletter.PROMO -> getString(R.string.profile_settings_newsletter_promo)
            Newsletter.WEEKLY -> getString(R.string.profile_settings_newsletter_weekly)
            else -> null
        }
    }

    private fun showOptInPrompt(newsletter: Newsletter) {
        val string = newsletterString(newsletter)

        val optInDialogMessageString = this.ksString.format(getString(R.string.profile_settings_newsletter_opt_in_message), "newsletter", string)
        ViewUtils.showDialog(this, getString(R.string.profile_settings_newsletter_opt_in_title), optInDialogMessageString)
    }
}
