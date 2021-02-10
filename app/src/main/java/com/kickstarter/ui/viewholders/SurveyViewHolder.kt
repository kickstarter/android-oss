package com.kickstarter.ui.viewholders

import android.content.Intent
import android.text.Html
import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ActivitySurveyViewBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.models.SurveyResponse
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.SurveyResponseActivity
import com.kickstarter.viewmodels.SurveyHolderViewModel
import com.squareup.picasso.Picasso

class SurveyViewHolder(private val binding: ActivitySurveyViewBinding) :
    KSViewHolder(binding.root) {
    private val ksString: KSString = environment().ksString()
    private val viewModel: SurveyHolderViewModel.ViewModel = SurveyHolderViewModel.ViewModel(environment())

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
        val surveyResponse = ObjectUtils.requireNonNull(data as SurveyResponse?)
        viewModel.inputs.configureWith(surveyResponse)
    }

    override fun onClick(view: View) {
        viewModel.inputs.surveyClicked()
    }

    private fun setCreatorAvatarImage(creatorAvatarImage: String) {
        Picasso.with(context())
            .load(creatorAvatarImage)
            .transform(CircleTransformation())
            .into(binding.surveyAvatarImage)
    }

    private fun startSurveyResponseActivity(surveyResponse: SurveyResponse) {
        val intent = Intent(context(), SurveyResponseActivity::class.java)
            .putExtra(IntentKey.SURVEY_RESPONSE, surveyResponse)
        context().startActivity(intent)
    }

    init {
        viewModel.outputs.creatorAvatarImageUrl()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setCreatorAvatarImage(it) }
        viewModel.outputs.creatorNameTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.surveyTitle.text = it }
        viewModel.outputs.projectForSurveyDescription()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setSurveyDescription(it) }
        viewModel.outputs.startSurveyResponseActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startSurveyResponseActivity(it) }
    }
}
