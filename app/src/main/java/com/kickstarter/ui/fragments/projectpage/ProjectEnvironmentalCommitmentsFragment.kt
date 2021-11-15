package com.kickstarter.ui.fragments.projectpage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.R
import com.kickstarter.databinding.FragmentProjectEnvironmentalCommitmentsBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Configure
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.parseHtmlTag
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.adapters.EnvironmentalCommitmentsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.makeLinks
import com.kickstarter.ui.extensions.parseHtmlTag
import com.kickstarter.viewmodels.projectpage.ProjectEnvironmentalCommitmentsViewModel

@RequiresFragmentViewModel(ProjectEnvironmentalCommitmentsViewModel.ViewModel::class)
class ProjectEnvironmentalCommitmentsFragment :
    BaseFragment<ProjectEnvironmentalCommitmentsViewModel.ViewModel>(),
    Configure {

    private var binding: FragmentProjectEnvironmentalCommitmentsBinding? = null

    private var environmentalCommitmentsAdapter = EnvironmentalCommitmentsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentProjectEnvironmentalCommitmentsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupVisitOurEnvironmentalResourcesCenter()

        this.viewModel.outputs.projectEnvironmentalCommitment()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                environmentalCommitmentsAdapter.takeData(it)
            }

        this.viewModel.outputs.openVisitOurEnvironmentalResourcesCenter()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                context?.let { context ->
                    ApplicationUtils.openUrlExternally(context, it)
                }
            }
    }

    private fun setupRecyclerView() {
        binding?.environmentalCommitmentsRecyclerView?.adapter = environmentalCommitmentsAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun setupVisitOurEnvironmentalResourcesCenter() {
        binding?.visitOurEnvironmentalResourcesCenterTv?.text =
            getString(R.string.Visit_our_Environmental_Resources_Center) + " " + getString(R.string
                .To_learn_how_Kickstarter_encourages_sustainable_practices)
        
        binding?.visitOurEnvironmentalResourcesCenterTv?.parseHtmlTag()
        binding?.visitOurEnvironmentalResourcesCenterTv?.makeLinks(
            Pair(
                getString(R.string.Visit_our_Environmental_Resources_Center),
                View.OnClickListener {
                    viewModel.inputs.onVisitOurEnvironmentalResourcesCenterClicked()
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
        fun newInstance(position: Int): ProjectEnvironmentalCommitmentsFragment {
            val fragment = ProjectEnvironmentalCommitmentsFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.PROJECT_PAGER_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
