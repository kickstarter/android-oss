package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Pair
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kickstarter.R
import com.kickstarter.features.projectstory.ProjectStoryViewModel
import com.kickstarter.features.socialshare.AndroidSocialShareService
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.ui.LocalSocialShareViewModel
import com.kickstarter.features.socialshare.ui.SocialShareSheet
import com.kickstarter.features.socialshare.viewmodel.SocialShareViewModel
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.KSString
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.EventContextValues.ContextPageName
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.startCreatorBioWebViewActivity
import com.kickstarter.viewmodels.projectpage.PrelaunchProjectViewModel
import com.kickstarter.viewmodels.projectpage.SimilarProjectsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PreLaunchProjectPageActivity : ComponentActivity() {

    private lateinit var viewModelFactory: PrelaunchProjectViewModel.Factory
    private val viewModel: PrelaunchProjectViewModel.PrelaunchProjectViewModel by viewModels { viewModelFactory }
    private lateinit var similarProjectsViewModelFactory: SimilarProjectsViewModel.Factory
    private val similarProjectsViewModel: SimilarProjectsViewModel by viewModels { similarProjectsViewModelFactory }
    private lateinit var projectStoryViewModelFactory: ProjectStoryViewModel.Factory
    private val projectStoryViewModel: ProjectStoryViewModel by viewModels { projectStoryViewModelFactory }
    private val compositeDisposable = CompositeDisposable()
    private var ksString: KSString? = null

    private val projectShareLabelString = R.string.project_accessibility_button_share_label
    private val projectShareCopyString = R.string.project_share_twitter_message
    private val projectStarConfirmationString = R.string.We_will_email_you

    private var darkModeEnabled = false
    private var theme = AppThemes.MATCH_SYSTEM.ordinal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.ksString = requireNotNull(getEnvironment()?.ksString())

        this.getEnvironment()?.let { env ->
            viewModelFactory = PrelaunchProjectViewModel.Factory(env)
            similarProjectsViewModelFactory = SimilarProjectsViewModel.Factory(env)
            projectStoryViewModelFactory = ProjectStoryViewModel.Factory(env)
            darkModeEnabled = env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            theme = env.sharedPreferences()
                ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                ?: AppThemes.MATCH_SYSTEM.ordinal
        }

        viewModel.inputs.configureWith(intent)

        setContent {
            KickstarterApp(
                useDarkTheme =
                if (darkModeEnabled) {
                    when (theme) {
                        AppThemes.MATCH_SYSTEM.ordinal -> isSystemInDarkTheme()
                        AppThemes.DARK.ordinal -> true
                        AppThemes.LIGHT.ordinal -> false
                        else -> false
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    isSystemInDarkTheme() // Force dark mode uses system theme
                } else false
            ) {
                val context = LocalContext.current
                val projectState = viewModel.project().subscribeAsState(initial = null)
                val similarProjectsState = similarProjectsViewModel.similarProjectsUiState.collectAsState()
                val projectStoryState = projectStoryViewModel.projectStoryUiState.collectAsState()
                var shareData: SocialShareData? by remember { mutableStateOf(null) }

                LaunchedEffect(projectState.value) {
                    projectState.value?.let { project ->
                        similarProjectsViewModel.provideProject(project)
                    }
                }

                val projectSlug = projectState.value?.slug()

                LaunchedEffect(projectSlug) {
                    projectSlug?.let { slug ->
                        projectStoryViewModel.provideProjectSlug(slug)
                        projectStoryViewModel.fetchProject()
                    }
                }

                PreLaunchProjectPageScreen(
                    projectState = projectState,
                    similarProjectsState = similarProjectsState,
                    projectStoryState = projectStoryState,
                    leftOnClickAction = { finish() },
                    rightOnClickAction = {
                        projectState.value?.let { this.viewModel.inputs.bookmarkButtonClicked() }
                    },
                    middleRightClickAction = {
                        if (viewModel.outputs.isNewSocialShareEnabled()) {
                            projectState.value?.let { project ->
                                shareData = SocialShareData(
                                    projectName = project.name() ?: "",
                                    projectUrl = project.urls()?.web()?.project() ?: "",
                                    imageUrl = project.photo()?.full() ?: "",
                                    creatorName = project.creator()?.name() ?: ""
                                )
                            }
                        } else {
                            this.viewModel.inputs.shareButtonClicked()
                        }
                    },
                    onCreatorLayoutClicked = { this.viewModel.inputs.creatorInfoClicked() },
                    onButtonClicked = {
                        projectState.value?.let { this.viewModel.inputs.bookmarkButtonClicked() }
                    },
                    numberOfFollowers = viewModel.environment.ksString()?.format(
                        getString(R.string.activity_followers),
                        "number_of_followers",
                        projectState.value?.watchesCount()?.toString()
                    ),
                    onSimilarProjectClick = { project ->
                        /* Same logic as in `ProjectOverviewFragment`. */
                        /* Currently, the VM only fetches Live Projects. If the VM's API call is
                         * is updated to include Pre-launch Projects, update this handler to check
                         * `Project.displayPrelaunch` and start `PreLaunchProjectPageActivity`. */
                        val intent = Intent().getProjectIntent(context).apply {
                            putExtra(IntentKey.PROJECT_PARAM, project.slug())
                            putExtra(IntentKey.REF_TAG, RefTag.similarProjects())
                        }
                        startActivity(intent)
                    }
                )

                shareData?.let { data ->
                    val shareViewModel: SocialShareViewModel = viewModel(
                        key = data.projectUrl,
                        factory = SocialShareViewModel.Factory(
                            environment = requireNotNull(getEnvironment()),
                            service = AndroidSocialShareService(context.applicationContext),
                            shareData = data,
                            contextPage = ContextPageName.PRE_LAUNCH_PROJECT
                        )
                    )
                    CompositionLocalProvider(LocalSocialShareViewModel provides shareViewModel) {
                        SocialShareSheet(
                            shareData = data,
                            isVisible = true,
                            onDismiss = { shareData = null },
                            onIntentReady = { startActivity(it) }
                        )
                    }
                }
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

    @Override
    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
