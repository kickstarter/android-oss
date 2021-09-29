package com.kickstarter.ui.fragments.projectpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.databinding.FragmentProjectEnvironmentalCommitmentsBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Configure
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.ProjectEnvironmentalCommitmentsViewModel

@RequiresFragmentViewModel(ProjectEnvironmentalCommitmentsViewModel.ViewModel::class)
class ProjectEnvironmentalCommitmentsFragment :
    BaseFragment<ProjectEnvironmentalCommitmentsViewModel.ViewModel>(),
    Configure {

    private var binding: FragmentProjectEnvironmentalCommitmentsBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentProjectEnvironmentalCommitmentsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
