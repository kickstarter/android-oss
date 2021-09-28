package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemFrequentlyAskedQuestionCardBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.ProjectFaq
import com.kickstarter.viewmodels.FrequentlyAskedQuestionsViewHolderViewModel

class FrequentlyAskedQuestionsViewHolder(
    val binding: ItemFrequentlyAskedQuestionCardBinding,
) : KSViewHolder(binding.root) {

    private val viewModel: FrequentlyAskedQuestionsViewHolderViewModel.ViewModel =
        FrequentlyAskedQuestionsViewHolderViewModel.ViewModel(environment())

    init {
        viewModel.outputs.question()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.questionAnswerLayout.setQuestion(it) }

        viewModel.outputs.answer()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.questionAnswerLayout.setAnswer(it) }

        viewModel.outputs.updatedDate()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.questionAnswerLayout.setLastedUpdateDate(it) }
    }

    override fun bindData(data: Any?) {
        this.viewModel.inputs.configureWith(data as ProjectFaq)
    }
}
