package com.kickstarter.ui.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.databinding.ThanksLayoutBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ThanksAdapter
import com.kickstarter.ui.extensions.showRatingDialogWidget
import com.kickstarter.viewmodels.ThanksViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class ThanksActivity : AppCompatActivity() {
    private lateinit var ksString: KSString
    private lateinit var binding: ThanksLayoutBinding
    private val projectStarConfirmationString = R.string.project_star_confirmation

    private lateinit var viewModelFactory: ThanksViewModel.Factory
    private val viewModel: ThanksViewModel.ThanksViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables = CompositeDisposable()
        this.getEnvironment()?.let { env ->
            viewModelFactory = ThanksViewModel.Factory(env)
        }
        binding = ThanksLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ksString = requireNotNull(getEnvironment()?.ksString())

        binding.thanksRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            orientation = RecyclerView.VERTICAL
        }

        val adapter = ThanksAdapter(viewModel.inputs)
        binding.thanksRecyclerView.adapter = adapter

        viewModel.outputs.adapterData()
            .compose(Transformers.observeForUIV2())
            .subscribe { adapter.takeData(it) }
            .addToDisposable(disposables)

        viewModel.outputs.showConfirmGamesNewsletterDialog()
            .compose(Transformers.observeForUIV2())
            .subscribe { showConfirmGamesNewsletterDialog() }
            .addToDisposable(disposables)

        viewModel.outputs.finish()
            .compose(Transformers.observeForUIV2())
            .subscribe { finish() }
            .addToDisposable(disposables)

        // I'm not sure why we would attempt to show a dialog after a delay but hopefully this helps
        viewModel.outputs.showGamesNewsletterDialog()
            .take(1)
            .delay(700L, TimeUnit.MILLISECONDS)
            .compose(Transformers.observeForUIV2())
            .subscribe {
                if (!isFinishing) {
                    showGamesNewsletterDialog()
                }
            }
            .addToDisposable(disposables)

        viewModel.outputs.showRatingDialog()
            .take(1)
            .delay(700L, TimeUnit.MILLISECONDS)
            .compose(Transformers.observeForUIV2())
            .subscribe {
                if (!isFinishing) {
                    showRatingDialog()
                }
            }
            .addToDisposable(disposables)

        viewModel.outputs.startDiscoveryActivity()
            .compose(Transformers.observeForUIV2())
            .subscribe { startDiscoveryActivity(it) }
            .addToDisposable(disposables)

        viewModel.outputs.startProjectActivity()
            .compose(Transformers.observeForUIV2())
            .subscribe { startProjectActivity(it) }
            .addToDisposable(disposables)

        binding.thanksToolbar.closeButton.setOnClickListener { closeButtonClick() }

        this.viewModel.outputs.showSavedPrompt()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.showStarToast() }
            .addToDisposable(disposables)

        viewModel.provideIntent(intent)
    }

    private fun showStarToast() {
        ViewUtils.showToastFromTop(this, getString(this.projectStarConfirmationString), 0, resources.getDimensionPixelSize(R.dimen.grid_8))
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        binding.thanksRecyclerView.adapter = null
    }

    private fun closeButtonClick() = viewModel.inputs.closeButtonClicked()

    private fun showConfirmGamesNewsletterDialog() {
        val optInDialogMessageString = ksString.format(
            getString(R.string.profile_settings_newsletter_opt_in_message),
            "newsletter",
            getString(R.string.profile_settings_newsletter_games)
        )

        val builder = AlertDialog.Builder(this)
            .setMessage(optInDialogMessageString)
            .setTitle(R.string.profile_settings_newsletter_opt_in_title)
            .setPositiveButton(R.string.general_alert_buttons_ok) { _: DialogInterface?, _: Int -> }
        builder.show()
    }

    private fun showGamesNewsletterDialog() {
        val builder = AlertDialog.Builder(this)
            .setMessage(R.string.project_checkout_games_alert_want_the_coolest_games_delivered_to_your_inbox)
            .setPositiveButton(R.string.project_checkout_games_alert_yes_please) { _: DialogInterface?, _: Int -> viewModel.inputs.signupToGamesNewsletterClick() }
            .setNegativeButton(R.string.project_checkout_games_alert_no_thanks) { _: DialogInterface?, _: Int -> }
        builder.show()
    }

    private fun showRatingDialog() = showRatingDialogWidget()

    private fun startDiscoveryActivity(params: DiscoveryParams) {
        val intent = Intent(this, DiscoveryActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .putExtra(IntentKey.DISCOVERY_PARAMS, params)
        startActivity(intent)
    }

    private fun startProjectActivity(projectAndRefTagAndIsFfEnabled: Pair<Project, RefTag>) {
        val intent = Intent().getProjectIntent(this)
            .putExtra(IntentKey.PROJECT_PARAM, projectAndRefTagAndIsFfEnabled.first?.slug())
            .putExtra(IntentKey.REF_TAG, projectAndRefTagAndIsFfEnabled.second)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
