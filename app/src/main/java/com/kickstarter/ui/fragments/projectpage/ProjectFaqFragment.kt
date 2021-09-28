package com.kickstarter.ui.fragments.projectpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.databinding.FragmentProjectFaqBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Configure
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.ProjectFaqViewModel

@RequiresFragmentViewModel(ProjectFaqViewModel.ViewModel::class)
class ProjectFaqFragment : BaseFragment<ProjectFaqViewModel.ViewModel>(), Configure {
    private var binding: FragmentProjectFaqBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentProjectFaqBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun configureWith(projectData: ProjectData) {
        this.viewModel?.inputs?.configureWith(projectData)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: FAQ's ready for complete https://kickstarter.atlassian.net/browse/NTV-209
        this.viewModel.outputs.projectFaqs()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                var faq = ""
                it.map { faq += "***QUESTION: ${it.question} \n ***ANSWER:${it.answer} \n" }
                binding?.placeholder?.text = faq
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): ProjectFaqFragment {
            val fragment = ProjectFaqFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.PROJECT_PAGER_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
