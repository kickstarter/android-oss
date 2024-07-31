package com.kickstarter.ui.viewholders

import android.content.Intent
import android.text.Html
import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySurveyViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Project
import com.kickstarter.models.SurveyResponse
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.SurveyResponseActivity
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.viewmodels.SurveyHolderViewModel
import io.reactivex.disposables.CompositeDisposable

class SurveyViewHolder(private val binding: ActivitySurveyViewBinding) :
    KSViewHolder(binding.root) {
    private val ksString = requireNotNull(environment().ksString())
    private val viewModel: SurveyHolderViewModel.ViewModel = SurveyHolderViewModel.ViewModel()
    private val disposables = CompositeDisposable()

    private fun setSurveyDescription(projectForSurveyDescription: Project) {
        binding.surveyText.text = Html.fromHtml(
            ksString.format(
                context().getString(R.string.Creator_name_needs_some_information_to_deliver_your_reward_for_project_name),
                "creator_name", projectForSurveyDescription.creator().name(),
                "project_name", projectForSurveyDescription.name()
            )
        )
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val surveyResponse = requireNotNull(data as SurveyResponse?)
        viewModel.inputs.configureWith(surveyResponse)
    }

    override fun onClick(view: View) {
        viewModel.inputs.surveyClicked()
    }

    private fun setCreatorAvatarImage(creatorAvatarImage: String) {
        binding.surveyAvatarImage.loadCircleImage(creatorAvatarImage)
    }

    private fun startSurveyResponseActivity(surveyResponse: SurveyResponse) {
        val intent = Intent(context(), SurveyResponseActivity::class.java)
            .putExtra(IntentKey.SURVEY_RESPONSE, surveyResponse)
        context().startActivity(intent)
    }

    init {
        viewModel.outputs.creatorAvatarImageUrl()
            .compose(Transformers.observeForUIV2())
            .subscribe { setCreatorAvatarImage(it) }
            .addToDisposable(disposables)
        viewModel.outputs.creatorNameTextViewText()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.surveyTitle.text = it }
            .addToDisposable(disposables)
        viewModel.outputs.projectForSurveyDescription()
            .compose(Transformers.observeForUIV2())
            .subscribe { setSurveyDescription(it) }
            .addToDisposable(disposables)
        viewModel.outputs.startSurveyResponseActivity()
            .compose(Transformers.observeForUIV2())
            .subscribe { startSurveyResponseActivity(it) }
            .addToDisposable(disposables)
    }

    override fun destroy() {
        disposables.clear()
        super.destroy()
    }
}
