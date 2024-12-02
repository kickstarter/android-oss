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
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.showRatingDialogWidget
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.ThanksViewModel
import com.kickstarter.viewmodels.ThanksViewModel.Factory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class ThanksActivity : AppCompatActivity() {
    private lateinit var factory: Factory
    private val viewModel: ThanksViewModel.ThanksViewModel by viewModels { factory }
    private lateinit var ksString: KSString
    private lateinit var binding: ThanksLayoutBinding
    private val projectStarConfirmationString = R.string.project_star_confirmation
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ThanksLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)
        setUpConnectivityStatusCheck(lifecycle)
        getEnvironment()?.let { env ->
            intent?.let {
                factory = Factory(env, it)
            }
            ksString = requireNotNull(env.ksString())
        }

        binding.thanksRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            orientation = RecyclerView.VERTICAL
        }

        val adapter = ThanksAdapter(viewModel.inputs)
        binding.thanksRecyclerView.adapter = adapter

        viewModel.outputs.adapterData()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.takeData(it) }
            .addToDisposable(disposables)

        viewModel.outputs.showConfirmGamesNewsletterDialog()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showConfirmGamesNewsletterDialog() }
            .addToDisposable(disposables)

        viewModel.outputs.finish()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { finish() }
            .addToDisposable(disposables)

        // I'm not sure why we would attempt to show a dialog after a delay but hopefully this helps
        viewModel.outputs.showGamesNewsletterDialog()
            .take(1)
            .delay(700L, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (!isFinishing) {
                    showGamesNewsletterDialog()
                }
            }
            .addToDisposable(disposables)

        viewModel.outputs.showRatingDialog()
            .take(1)
            .delay(700L, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
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
    }

    private fun showStarToast() {
        ViewUtils.showToastFromTop(this, getString(this.projectStarConfirmationString), 0, resources.getDimensionPixelSize(R.dimen.grid_8))
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
        binding.thanksRecyclerView.adapter = null
    }

    private fun closeButtonClick() = viewModel.inputs.closeButtonClicked()

    private fun showConfirmGamesNewsletterDialog() {
        this.runOnUiThread(
            Runnable {
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
        )
    }

    private fun showGamesNewsletterDialog() {
        this.runOnUiThread(
            Runnable {
                val builder = AlertDialog.Builder(this)
                    .setMessage(R.string.project_checkout_games_alert_want_the_coolest_games_delivered_to_your_inbox)
                    .setPositiveButton(R.string.project_checkout_games_alert_yes_please) { _: DialogInterface?, _: Int -> viewModel.inputs.signupToGamesNewsletterClick() }
                    .setNegativeButton(R.string.project_checkout_games_alert_no_thanks) { _: DialogInterface?, _: Int -> }
                builder.show()
            }
        )
    }

    private fun showRatingDialog() = showRatingDialogWidget()

    private fun startDiscoveryActivity(params: DiscoveryParams) {
        val intent = Intent(this, DiscoveryActivity::class.java)
            .putExtra(IntentKey.DISCOVERY_PARAMS, params)
        startActivity(intent)
    }

    private fun startProjectActivity(projectAndRefTagAndIsFfEnabled: Pair<Project, RefTag>) {
        val intent = Intent().getProjectIntent(this)
            .putExtra(IntentKey.PROJECT_PARAM, projectAndRefTagAndIsFfEnabled.first?.slug())
            .putExtra(IntentKey.REF_TAG, projectAndRefTagAndIsFfEnabled.second)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
