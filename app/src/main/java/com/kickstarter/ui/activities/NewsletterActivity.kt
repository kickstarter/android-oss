package com.kickstarter.ui.activities

import android.os.Bundle
import android.support.annotation.NonNull
import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.KSString
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.BooleanUtils.isTrue
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.TransitionUtils
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

        this.viewModel.outputs.subscribeAll()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { SwitchCompatUtils.setCheckedWithoutAnimation(subscribe_all_switch, it) }

        alumni_switch.setOnClickListener { viewModel.inputs.sendAlumniNewsletter(alumni_switch.isChecked) }
        arts_news_switch.setOnClickListener { viewModel.inputs.sendArtsNewsNewsletter(arts_news_switch.isChecked) }
        films_switch.setOnClickListener { viewModel.inputs.sendFilmsNewsletter(films_switch.isChecked) }
        games_we_love_switch.setOnClickListener { viewModel.inputs.sendGamesNewsletter(games_we_love_switch.isChecked) }
        happening_switch.setOnClickListener { viewModel.inputs.sendHappeningNewsletter(happening_switch.isChecked) }
        invent_switch.setOnClickListener { viewModel.inputs.sendInventNewsletter(invent_switch.isChecked) }
        music_switch.setOnClickListener { viewModel.inputs.sendMusicNewsletter(music_switch.isChecked) }
        news_events_switch.setOnClickListener { viewModel.inputs.sendPromoNewsletter(news_events_switch.isChecked) }
        projects_we_love_switch.setOnClickListener { viewModel.inputs.sendWeeklyNewsletter(projects_we_love_switch.isChecked) }
        reads_switch.setOnClickListener { viewModel.inputs.sendReadsNewsletter(reads_switch.isChecked) }
        subscribe_all_switch.setOnClickListener { viewModel.inputs.sendAllNewsletter(subscribe_all_switch.isChecked) }
    }

    override fun exitTransition(): Pair<Int, Int> = TransitionUtils.slideUpFromBottom()

    private fun displayPreferences(@NonNull user: User) {
        SwitchCompatUtils.setCheckedWithoutAnimation(alumni_switch, isTrue(user.alumniNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(arts_news_switch, isTrue(user.artsCultureNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(films_switch, isTrue(user.filmNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(games_we_love_switch, isTrue(user.gamesNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(happening_switch, isTrue(user.happeningNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(invent_switch, isTrue(user.inventNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(music_switch, isTrue(user.musicNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(news_events_switch, isTrue(user.promoNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(projects_we_love_switch, isTrue(user.weeklyNewsletter()))
        SwitchCompatUtils.setCheckedWithoutAnimation(reads_switch, isTrue(user.publishingNewsletter()))
    }

    private fun newsletterString(@NonNull newsletter: Newsletter): String? {
        return when (newsletter) {
            Newsletter.ALL -> getString(R.string.profile_settings_newsletter_subscribe_all)
            Newsletter.ALUMNI -> getString(R.string.profile_settings_newsletter_alumni)
            Newsletter.ARTS -> getString(R.string.profile_settings_newsletter_arts)
            Newsletter.FILMS -> getString(R.string.profile_settings_newsletter_film)
            Newsletter.GAMES -> getString(R.string.profile_settings_newsletter_games)
            Newsletter.HAPPENING -> getString(R.string.profile_settings_newsletter_happening)
            Newsletter.INVENT -> getString(R.string.profile_settings_newsletter_invent)
            Newsletter.MUSIC -> getString(R.string.Its_like_the_radio_but_nothing_sucks_and_also_its_a_newsletter)
            Newsletter.PROMO -> getString(R.string.profile_settings_newsletter_promo)
            Newsletter.READS -> getString(R.string.profile_settings_newsletter_publishing)
            Newsletter.WEEKLY -> getString(R.string.profile_settings_newsletter_weekly)
        }
    }

    private fun showOptInPrompt(newsletter: Newsletter) {
        val string = newsletterString(newsletter)

        val optInDialogMessageString = this.ksString.format(getString(R.string.profile_settings_newsletter_opt_in_message), "newsletter", string)
        ViewUtils.showDialog(this, getString(R.string.profile_settings_newsletter_opt_in_title), optInDialogMessageString)
    }
}
