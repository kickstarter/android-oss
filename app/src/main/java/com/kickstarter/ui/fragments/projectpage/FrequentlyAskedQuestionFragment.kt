package com.kickstarter.ui.fragments.projectpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.kickstarter.databinding.FragmentFrequentlyAskedQuestionBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Configure
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.adapters.FrequentlyAskedQuestionsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.projectpage.FrequentlyAskedQuestionViewModel

@RequiresFragmentViewModel(FrequentlyAskedQuestionViewModel.ViewModel::class)
class FrequentlyAskedQuestionFragment :
    BaseFragment<FrequentlyAskedQuestionViewModel.ViewModel>(),
    Configure {
    private var binding: FragmentFrequentlyAskedQuestionBinding? = null
    private var fqaAdapter = FrequentlyAskedQuestionsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentFrequentlyAskedQuestionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        this.viewModel.outputs.projectFaqList()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.answerEmptyStateTv?.isVisible = it.isEmpty()
                binding?.fqaRecyclerView?.isGone = it.isEmpty()
                fqaAdapter.takeData(it)
            }

        this.viewModel.outputs.bindEmptyState()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.answerEmptyStateTv?.isVisible = true
                binding?.fqaRecyclerView?.isGone = true
            }
    }

    private fun setupRecyclerView() {
        binding?.fqaRecyclerView?.adapter = fqaAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): FrequentlyAskedQuestionFragment {
            val fragment = FrequentlyAskedQuestionFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.PROJECT_PAGER_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun configureWith(projectData: ProjectData) {
        this.viewModel?.inputs?.configureWith(projectData.project().projectFaqs())
    }
}
