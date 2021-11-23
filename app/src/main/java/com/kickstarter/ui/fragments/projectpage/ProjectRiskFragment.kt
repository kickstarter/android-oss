package com.kickstarter.ui.fragments.projectpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.R
import com.kickstarter.databinding.FragmentProjectRisksBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Configure
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.makeLinks
import com.kickstarter.ui.extensions.parseHtmlTag
import com.kickstarter.viewmodels.projectpage.ProjectRiskViewModel

@RequiresFragmentViewModel(ProjectRiskViewModel.ViewModel::class)
class ProjectRiskFragment :
    BaseFragment<ProjectRiskViewModel.ViewModel>(),
    Configure {
    private var binding: FragmentProjectRisksBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentProjectRisksBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLearnAboutAccountabilityOnKickstarter()

        this.viewModel.outputs.projectRisks()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.riskSectionDescription?.text = it
            }

        this.viewModel.outputs.openLearnAboutAccountabilityOnKickstarter()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                context?.let { context ->
                    ApplicationUtils.openUrlExternally(context, it)
                }
            }
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
        this.viewModel?.inputs?.configureWith(projectData)
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
