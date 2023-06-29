package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemFrequentlyAskedQuestionCardBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.ProjectFaq
import com.kickstarter.viewmodels.FrequentlyAskedQuestionsViewHolderViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class FrequentlyAskedQuestionsViewHolder(
    val binding: ItemFrequentlyAskedQuestionCardBinding
) : KSViewHolder(binding.root) {

    private val viewModel: FrequentlyAskedQuestionsViewHolderViewModel.FrequentlyAskedQuestionsViewHolderViewModel =
        FrequentlyAskedQuestionsViewHolderViewModel.FrequentlyAskedQuestionsViewHolderViewModel()
    private val disposables = CompositeDisposable()

    init {
        viewModel.outputs.question()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.questionAnswerLayout.setQuestion(it) }
            .addToDisposable(disposables)

        viewModel.outputs.answer()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.questionAnswerLayout.setAnswer(it) }
            .addToDisposable(disposables)

        viewModel.outputs.updatedDate()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.questionAnswerLayout.setLastedUpdateDate(it) }
            .addToDisposable(disposables)
    }

    override fun bindData(data: Any?) {
        this.viewModel.inputs.configureWith(data as ProjectFaq)
    }

    override fun destroy() {
        this.viewModel.inputs.onDestroy()
        disposables.clear()
        super.destroy()
    }
}
