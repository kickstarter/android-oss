package com.kickstarter.ui.fragments.projectpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kickstarter.libs.Configure
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.ProjectAIViewModel
import io.reactivex.disposables.CompositeDisposable

class ProjectAIFragment :
    Fragment(),
    Configure {

    private lateinit var viewModelFactory: ProjectAIViewModel.Factory
    private val viewModel: ProjectAIViewModel.ProjectAIViewModel by viewModels { viewModelFactory }

    private var disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = ProjectAIViewModel.Factory(env)
        }

        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's TreeLifecycle is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            // Compose world
            setContent {
                MaterialTheme {
                    // TODO: MBL-901, empty for now
                }
            }
        }
    }

    override fun configureWith(projectData: ProjectData) {
        this.viewModel.inputs.configureWith(projectData)
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
