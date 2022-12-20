package com.kickstarter.ui.fragments.projectpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString

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
                    RisksScreen()
                }
            }
        }
        //return binding?.root
    }

    @Composable
    fun RisksScreen(
        modifier: Modifier = Modifier,
        riskDescription: String = ""
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(id = R.string.Risks_and_challenges),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .paddingFromBaseline(
                        top = dimensionResource(id = R.dimen.grid_3),
                        bottom = dimensionResource(id = R.dimen.grid_4)
                    )
                    .padding(horizontal = dimensionResource(id = R.dimen.grid_3))
            )
            Text(
                text = riskDescription,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .paddingFromBaseline(
                        top = dimensionResource(id = R.dimen.grid_3),
                        bottom = dimensionResource(id = R.dimen.grid_4)
                    )
                    .padding(horizontal = dimensionResource(id = R.dimen.grid_3))
            )
            Spacer(
                modifier = Modifier
                    .height(3.dp)
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(id = R.dimen.grid_3)
                    )
                    .background(
                        color = colorResource(id = R.color.kds_support_300)
                    )
            )
            ClickableText(
                text = AnnotatedString(stringResource(id = R.string.Learn_about_accountability_on_Kickstarter)),
                onClick = {
                    viewModel.onLearnAboutAccountabilityOnKickstarterClicked()
                },
                modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.grid_3))
                    .paddingFromBaseline(
                        top = dimensionResource(id = R.dimen.grid_3),
                        bottom = dimensionResource(id = R.dimen.grid_5)
                    )
            )
        }
    }


    @Preview(showBackground = true, backgroundColor = 0xFFF0EAE2)
    @Composable
    fun ProjectRisksPreview() {
        RisksScreen(
            riskDescription = "blablablabla blablablabblabla"
        )
    }

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
