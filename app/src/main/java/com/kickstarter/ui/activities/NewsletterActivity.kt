package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.R
import com.kickstarter.databinding.ActivityNewsletterBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.User
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.NewsletterViewModel.Factory
import com.kickstarter.viewmodels.NewsletterViewModel.NewsletterViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

enum class Newsletter {
    ALL, ALUMNI, ARTS, FILMS, GAMES, HAPPENING, INVENT, MUSIC, PROMO, READS, WEEKLY
}

class NewsletterActivity : AppCompatActivity() {

    private lateinit var ksString: KSString

    private lateinit var binding: ActivityNewsletterBinding

    private lateinit var viewModelFactory: Factory
    private val viewModel: NewsletterViewModel by viewModels {
        viewModelFactory
    }

    private val disposables = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsletterBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)

        setUpConnectivityStatusCheck(lifecycle)

        val environment = this.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env)
            env
        }

        this.ksString = requireNotNull(environment?.ksString())

        this.viewModel.outputs.user()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.displayPreferences(it) }
            .addToDisposable(disposables)

        this.viewModel.errors.unableToSavePreferenceError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showToast(this, getString(R.string.profile_settings_error)) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showOptInPrompt()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::showOptInPrompt)
            .addToDisposable(disposables)

        this.viewModel.outputs.subscribeAll()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                SwitchCompatUtils.setCheckedWithoutAnimation(
                    binding.subscribeAllSwitch,
                    it
                )
            }
            .addToDisposable(disposables)

        binding.alumniSwitch.setOnClickListener { viewModel.inputs.sendAlumniNewsletter(binding.alumniSwitch.isChecked) }
        binding.artsNewsSwitch.setOnClickListener { viewModel.inputs.sendArtsNewsNewsletter(binding.artsNewsSwitch.isChecked) }
        binding.filmsSwitch.setOnClickListener { viewModel.inputs.sendFilmsNewsletter(binding.filmsSwitch.isChecked) }
        binding.gamesWeLoveSwitch.setOnClickListener { viewModel.inputs.sendGamesNewsletter(binding.gamesWeLoveSwitch.isChecked) }
        binding.happeningSwitch.setOnClickListener {
            viewModel.inputs.sendHappeningNewsletter(
                binding.happeningSwitch.isChecked
            )
        }
        binding.inventSwitch.setOnClickListener { viewModel.inputs.sendInventNewsletter(binding.inventSwitch.isChecked) }
        binding.musicSwitch.setOnClickListener { viewModel.inputs.sendMusicNewsletter(binding.musicSwitch.isChecked) }
        binding.newsEventsSwitch.setOnClickListener { viewModel.inputs.sendPromoNewsletter(binding.newsEventsSwitch.isChecked) }
        binding.projectsWeLoveSwitch.setOnClickListener {
            viewModel.inputs.sendWeeklyNewsletter(
                binding.projectsWeLoveSwitch.isChecked
            )
        }
        binding.readsSwitch.setOnClickListener { viewModel.inputs.sendReadsNewsletter(binding.readsSwitch.isChecked) }
        binding.subscribeAllSwitch.setOnClickListener { viewModel.inputs.sendAllNewsletter(binding.subscribeAllSwitch.isChecked) }
    }

    private fun displayPreferences(user: User) {
        SwitchCompatUtils.setCheckedWithoutAnimation(
            binding.alumniSwitch,
            user.alumniNewsletter().isTrue()
        )
        SwitchCompatUtils.setCheckedWithoutAnimation(
            binding.artsNewsSwitch,
            user.artsCultureNewsletter().isTrue()
        )
        SwitchCompatUtils.setCheckedWithoutAnimation(
            binding.filmsSwitch,
            user.filmNewsletter().isTrue()
        )
        SwitchCompatUtils.setCheckedWithoutAnimation(
            binding.gamesWeLoveSwitch,
            user.gamesNewsletter().isTrue()
        )
        SwitchCompatUtils.setCheckedWithoutAnimation(
            binding.happeningSwitch,
            user.happeningNewsletter().isTrue()
        )
        SwitchCompatUtils.setCheckedWithoutAnimation(
            binding.inventSwitch,
            user.inventNewsletter().isTrue()
        )
        SwitchCompatUtils.setCheckedWithoutAnimation(
            binding.musicSwitch,
            user.musicNewsletter().isTrue()
        )
        SwitchCompatUtils.setCheckedWithoutAnimation(
            binding.newsEventsSwitch,
            user.promoNewsletter().isTrue()
        )
        SwitchCompatUtils.setCheckedWithoutAnimation(
            binding.projectsWeLoveSwitch,
            user.weeklyNewsletter().isTrue()
        )
        SwitchCompatUtils.setCheckedWithoutAnimation(
            binding.readsSwitch,
            user.publishingNewsletter().isTrue()
        )
    }

    private fun newsletterString(newsletter: Newsletter): String? {
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

        val optInDialogMessageString = this.ksString.format(
            getString(R.string.profile_settings_newsletter_opt_in_message),
            "newsletter",
            string
        )
        ViewUtils.showDialog(
            this,
            getString(R.string.profile_settings_newsletter_opt_in_title),
            optInDialogMessageString
        )
    }
}
