package com.kickstarter.ui.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.NonNull
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

        build = environment().build()
        currentUserType =  environment().currentUser()
        ksString = environment().ksString()

        viewModel.outputs.user()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::displayPreferences)

        viewModel.errors.unableToSavePreferenceError()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ViewUtils.showToast(this, getString(R.string.profile_settings_error)) }

        RxView.clicks(binding.happeningSwitch)
                .compose(bindToLifecycle())
                .subscribe { viewModel.inputs.sendHappeningNewsletter(binding.happeningSwitch.isChecked) }

        RxView.clicks(binding.newsEventsSwitch)
                .compose(bindToLifecycle())
                .subscribe { viewModel.inputs.sendPromoNewsletter(binding.newsEventsSwitch.isChecked) }

        RxView.clicks(binding.projectsWeLoveTextView)
                .compose(bindToLifecycle())
                .subscribe { viewModel.inputs.sendWeeklyNewsletter(binding.projectsWeLoveSwitch.isChecked) }

    }

    private fun displayPreferences(@NonNull user: User) {
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.happeningSwitch, isTrue(user.happeningNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.newsEventsSwitch, isTrue(user.promoNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.projectsWeLoveSwitch, isTrue(user.weeklyNewsletter()))
    }
}
