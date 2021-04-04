package com.kickstarter.ui.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.databinding.ThanksLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ThanksAdapter
import com.kickstarter.viewmodels.ThanksViewModel
import java.util.concurrent.TimeUnit

@RequiresActivityViewModel(ThanksViewModel.ViewModel::class)
class ThanksActivity : BaseActivity<ThanksViewModel.ViewModel>() {
    private lateinit var ksString: KSString
    private lateinit var binding: ThanksLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ThanksLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ksString = environment().ksString()

        binding.thanksRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            orientation = RecyclerView.VERTICAL
        }

        val adapter = ThanksAdapter(viewModel.inputs)
        binding.thanksRecyclerView.adapter = adapter

        viewModel.outputs.adapterData()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { adapter.takeData(it) }

        viewModel.outputs.showConfirmGamesNewsletterDialog()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { showConfirmGamesNewsletterDialog() }

        viewModel.outputs.finish()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { finish() }

        // I'm not sure why we would attempt to show a dialog after a delay but hopefully this helps
        viewModel.outputs.showGamesNewsletterDialog()
            .compose(bindToLifecycle())
            .take(1)
            .delay(700L, TimeUnit.MILLISECONDS)
            .compose(Transformers.observeForUI())
            .subscribe {
                if (!isFinishing) {
                    showGamesNewsletterDialog()
                }
            }

        viewModel.outputs.showRatingDialog()
            .compose(bindToLifecycle())
            .take(1)
            .delay(700L, TimeUnit.MILLISECONDS)
            .compose(Transformers.observeForUI())
            .subscribe {
                if (!isFinishing) {
                    showRatingDialog()
                }
            }

        viewModel.outputs.startDiscoveryActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startDiscoveryActivity(it) }

        viewModel.outputs.startProjectActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startProjectActivity(it) }

        binding.thanksToolbar.closeButton.setOnClickListener { closeButtonClick() }
    }

    override fun onDestroy() {
        super.onDestroy()
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

    private fun showRatingDialog() = ViewUtils.showRatingDialog(this)

    private fun startDiscoveryActivity(params: DiscoveryParams) {
        val intent = Intent(this, DiscoveryActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .putExtra(IntentKey.DISCOVERY_PARAMS, params)
        startActivity(intent)
    }

    private fun startProjectActivity(projectAndRefTag: Pair<Project, RefTag>) {
        val intent = Intent(this, ProjectActivity::class.java)
            .putExtra(IntentKey.PROJECT, projectAndRefTag.first)
            .putExtra(IntentKey.REF_TAG, projectAndRefTag.second)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
