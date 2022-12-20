package com.kickstarter.ui.fragments.projectpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kickstarter.R
import com.kickstarter.databinding.FragmentProjectRisksBinding
import com.kickstarter.libs.Configure
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.makeLinks
import com.kickstarter.ui.extensions.parseHtmlTag
import com.kickstarter.viewmodels.projectpage.ProjectRiskViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ProjectRiskFragment :
    Fragment(),
    Configure {
    private var binding: FragmentProjectRisksBinding? = null

    private lateinit var viewModelFactory: ProjectRiskViewModel.Factory
    private val viewModel: ProjectRiskViewModel.ProjectRiskViewModel by viewModels { viewModelFactory }

    private var disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = ProjectRiskViewModel.Factory(env)
        }

        //val view = binding?.root
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // In Compose world
                MaterialTheme {
                    Surface(
                        color = Color.Blue,
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                    ) {
                        Text(text = "First UI loading built with Compose YAI!!!",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .paddingFromBaseline(top = 200.dp)
                        )
                    }
                }
            }
        }
        //return binding?.root
    }

    /*
    @Composable
    fun ProjectRiskElement(
        modifier: Modifier = Modifier
    ) {
        Surface(
            color = Color.Blue,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            Text(text = "First UI loading built with Compose YAI!!!",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .paddingFromBaseline(top = 40.dp)
            )
        }
    }

    @Preview(showBackground = true, backgroundColor = 0xFFF0EAE2)
    @Composable
    fun ProjectRisksPreview() {
        ProjectRiskElement()
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLearnAboutAccountabilityOnKickstarter()

        disposables.add(
            this.viewModel.outputs.projectRisks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    binding?.riskSectionDescription?.text = it
                }
        )

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

    private fun setupLearnAboutAccountabilityOnKickstarter() {
        binding?.learnAboutAccountabilityOnKickstarterTv?.parseHtmlTag()
        binding?.learnAboutAccountabilityOnKickstarterTv?.makeLinks(
            Pair(
                getString(R.string.Learn_about_accountability_on_Kickstarter),
                View.OnClickListener {
                    viewModel.inputs.onLearnAboutAccountabilityOnKickstarterClicked()
                }
            ),
            linkColor = R.color.kds_create_700,
            isUnderlineText = true
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
