package com.kickstarter.ui.fragments.projectpage

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kickstarter.libs.Configure
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.AppThemes
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.projectpage.ui.RisksScreen
import com.kickstarter.viewmodels.projectpage.ProjectRiskViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ProjectRiskFragment :
    Fragment(),
    Configure {

    private lateinit var viewModelFactory: ProjectRiskViewModel.Factory
    private val viewModel: ProjectRiskViewModel.ProjectRiskViewModel by viewModels { viewModelFactory }

    private var disposables = CompositeDisposable()

    private var darkModeEnabled = false
    private var theme = AppThemes.MATCH_SYSTEM.ordinal

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = ProjectRiskViewModel.Factory(env)
            darkModeEnabled =
                env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            theme = env.sharedPreferences()
                ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
                ?: AppThemes.MATCH_SYSTEM.ordinal
        }

        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            // Compose world
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
                    RisksScreen(
                        riskDescState = viewModel.projectRisks().subscribeAsState(initial = ""),
                        callback = {
                            viewModel.onLearnAboutAccountabilityOnKickstarterClicked()
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disposables.add(
            this.viewModel.outputs.openLearnAboutAccountabilityOnKickstarter()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    context?.let { context ->
                        ApplicationUtils.openUrlExternally(context, it)
                    }
                }
        )
    }

    override fun configureWith(projectData: ProjectData) {
        this.viewModel.inputs.configureWith(projectData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): ProjectRiskFragment {
            val fragment = ProjectRiskFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.PROJECT_PAGER_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
