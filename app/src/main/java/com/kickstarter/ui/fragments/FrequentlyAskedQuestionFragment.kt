package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.databinding.FragmentFrequentlyAskedQuestionBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.ProjectFaq
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.adapters.FrequentlyAskedQuestionsAdapter
import com.kickstarter.viewmodels.FrequentlyAskedQuestionViewModel

@RequiresFragmentViewModel(FrequentlyAskedQuestionViewModel.ViewModel::class)
class FrequentlyAskedQuestionFragment : BaseFragment<FrequentlyAskedQuestionViewModel.ViewModel>() {
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
            .subscribe { fqaAdapter.takeData(it) }
    }

    private fun setupRecyclerView() {
        binding?.fqaRecyclerView?.adapter = fqaAdapter
    }

    companion object {
        fun newInstance(projectFaqList: ArrayList<ProjectFaq>):
            FrequentlyAskedQuestionFragment {
                val fragment = FrequentlyAskedQuestionFragment()
                val argument = Bundle()
                argument.putParcelableArrayList(ArgumentsKey.PROJECT_QUESTIONS_ANSWERS, projectFaqList)
                fragment.arguments = argument
                return fragment
            }
    }
}
