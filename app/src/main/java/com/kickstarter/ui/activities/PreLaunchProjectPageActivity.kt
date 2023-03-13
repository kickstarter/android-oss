package com.kickstarter.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.rxjava2.subscribeAsState
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getCreatorBioWebViewActivityIntent
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreen
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.startCreatorBioWebViewActivity
import com.kickstarter.viewmodels.projectpage.PrelaunchProjectViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PreLaunchProjectPageActivity : ComponentActivity() {

    private lateinit var viewModelFactory: PrelaunchProjectViewModel.Factory
    private val viewModel: PrelaunchProjectViewModel.PrelaunchProjectViewModel by viewModels { viewModelFactory }
    private val compositeDisposable = CompositeDisposable()
    private var ksString: KSString? = null

    private val projectShareLabelString = R.string.project_accessibility_button_share_label
    private val projectShareCopyString = R.string.project_share_twitter_message
    private val projectStarConfirmationString = R.string.We_will_email_you

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.ksString = requireNotNull(getEnvironment()?.ksString())

        this.getEnvironment()?.let { env ->
            viewModelFactory = PrelaunchProjectViewModel.Factory(env)
        }

        viewModel.inputs.configureWith(intent)

        setContent {
            MaterialTheme {
                val projectState = viewModel.project().subscribeAsState(initial = null)

                PreLaunchProjectPageScreen(
                    projectState = projectState,
                    leftOnClickAction = { finish() },
                    rightOnClickAction = {
                        projectState.value?.let { this.viewModel.inputs.bookmarkButtonClicked() }
                    },
                    middleRightClickAction = { this.viewModel.inputs.shareButtonClicked() },
                    onCreatorLayoutClicked = { this.viewModel.inputs.creatorInfoClicked() },
                    onButtonClicked = {
                        projectState.value?.let { this.viewModel.inputs.bookmarkButtonClicked() }
                    },
                    numberOfFollowers =
                    viewModel.environment.ksString()?.format(
                        getString(R.string.activity_followers),
                        "number_of_followers",
                        projectState.value?.watchesCount()?.toString()
                    )

                )
            }
        }

        this.viewModel.outputs.showShareSheet()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startShareIntent(it)
            }.addToDisposable(compositeDisposable)

        this.viewModel.outputs.startLoginToutActivity()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.startLoginToutActivity() }
            .addToDisposable(compositeDisposable)

        this.viewModel.outputs.showSavedPrompt()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.showStarToast() }
            .addToDisposable(compositeDisposable)

        viewModel.outputs.startCreatorView()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startCreatorBioWebViewActivity(it)
            }.addToDisposable(compositeDisposable)
    }

    private fun showStarToast() {
        ViewUtils.showToast(this, getString(this.projectStarConfirmationString))
    }

    private fun startShareIntent(projectNameAndShareUrl: Pair<String, String>) {
        val name = projectNameAndShareUrl.first
        val shareMessage = this.ksString?.format(getString(this.projectShareCopyString), "project_title", name)

        val url = projectNameAndShareUrl.second
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "$shareMessage $url")
        startActivity(Intent.createChooser(intent, getString(this.projectShareLabelString)))
    }

    private fun startLoginToutActivity() {
        val intent = Intent(this, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.STAR_PROJECT)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
    }

    fun Activity.startCreatorBioWebViewActivity(project: Project) {
        startActivity(Intent().getCreatorBioWebViewActivityIntent(this, project))
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    @Override
    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
