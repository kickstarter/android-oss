package com.kickstarter.ui.fragments.projectpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kickstarter.libs.Configure
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.activities.compose.projectpage.AiDisclosureScreen
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.ProjectAIViewModel
import com.kickstarter.viewmodels.projectpage.ProjectAIViewModel.Event

class ProjectAIFragment :
    Fragment(),
    Configure {

    private lateinit var viewModelFactory: ProjectAIViewModel.Factory
    private val viewModel: ProjectAIViewModel by viewModels { viewModelFactory }

    private var darkModeEnabled = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = ProjectAIViewModel.Factory(env)
            // darkModeEnabled = env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
            darkModeEnabled = false
        }

        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's TreeLifecycle is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            // Compose world
            setContent {
                KickstarterApp(useDarkTheme = if (darkModeEnabled) isSystemInDarkTheme() else false) {
                    AiDisclosureScreen(
                        state = viewModel.state,
                        clickCallback = { url ->
                            ApplicationUtils.openUrlExternally(context, url)
                        }
                    )
                }
            }
        }
    }

    override fun configureWith(projectData: ProjectData) {
        this.viewModel.eventUpdate(Event(projectData = projectData))
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): ProjectAIFragment {
            val fragment = ProjectAIFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.PROJECT_PAGER_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
