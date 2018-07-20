package com.kickstarter.ui.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.NonNull
import butterknife.BindColor
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.ActivityNewsletterBinding
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
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(NewsletterViewModel.ViewModel::class)
class NewsletterActivity : BaseActivity<NewsletterViewModel.ViewModel>() {

    private lateinit var binding: ActivityNewsletterBinding

    private lateinit var currentUserType: CurrentUserType
    private lateinit var build: Build
    private lateinit var ksString: KSString

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_newsletter)

        this.build = environment().build()
        this.currentUserType =  environment().currentUser()
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

        RxView.clicks(this.binding.happeningSwitch)
                .compose(bindToLifecycle())
                .subscribe { viewModel.inputs.sendHappeningNewsletter(this.binding.happeningSwitch.isChecked) }

        RxView.clicks(this.binding.newsEventsSwitch)
                .compose(bindToLifecycle())
                .subscribe { viewModel.inputs.sendPromoNewsletter(this.binding.newsEventsSwitch.isChecked) }

        RxView.clicks(this.binding.projectsWeLoveTextView)
                .compose(bindToLifecycle())
                .subscribe { viewModel.inputs.sendWeeklyNewsletter(this.binding.projectsWeLoveSwitch.isChecked) }

    }

    private fun displayPreferences(@NonNull user: User) {
        SwitchCompatUtils.setCheckedWithoutAnimation(this.binding.happeningSwitch, isTrue(user.happeningNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(this.binding.newsEventsSwitch, isTrue(user.promoNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(this.binding.projectsWeLoveSwitch, isTrue(user.weeklyNewsletter()))
    }

    private fun newsletterString(newsletter: Newsletter): String? {
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
