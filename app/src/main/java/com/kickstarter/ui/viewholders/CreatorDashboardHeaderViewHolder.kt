package com.kickstarter.ui.viewholders

import android.content.Intent
import androidx.core.content.ContextCompat
import com.kickstarter.R
import com.kickstarter.databinding.DashboardFundingViewBinding
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.MessageThreadsActivity
import com.kickstarter.ui.activities.ProjectActivity
import com.kickstarter.ui.adapters.data.ProjectDashboardData
import com.kickstarter.viewmodels.CreatorDashboardHeaderHolderViewModel

class CreatorDashboardHeaderViewHolder(
    private val binding: DashboardFundingViewBinding,
    private val delegate: Delegate?
) : KSViewHolder(binding.root) {

    private val viewModel = CreatorDashboardHeaderHolderViewModel.ViewModel(environment())
    private val ksString = environment().ksString()
    private val ksCurrency = environment().ksCurrency()

    interface Delegate {
        fun projectsListButtonClicked()
    }
    init {
        viewModel.outputs.currentProject()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setTimeRemainingTextTextView(it) }
        viewModel.outputs.currentProject()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setPledgedOfGoalString(it) }
        viewModel.outputs.messagesButtonIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { ViewUtils.setGone(binding.creatorDashboardMessages, it) }
        viewModel.outputs.otherProjectsButtonIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.creatorDashboardProjectSelector))
        viewModel.outputs.percentageFunded()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.creatorDashboardPercentText.text = it }
        viewModel.outputs.percentageFundedProgress()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.creatorDashboardFunded.progress = it }
        viewModel.outputs.progressBarBackground()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.creatorDashboardFunded.progressDrawable = ContextCompat.getDrawable(context(), it) }
        viewModel.outputs.projectBackersCountText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.creatorDashboardBackerCount.text = it }
        viewModel.outputs.timeRemainingText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.creatorDashboardTimeRemaining.text = it }
        viewModel.outputs.startMessageThreadsActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startMessageThreadsActivity(it.first, it.second) }
        viewModel.outputs.startProjectActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startProjectActivity(it.first, it.second) }
        viewModel.outputs.viewProjectButtonIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.creatorViewProjectButton))
    }

    override fun bindData(data: Any?) {
        val projectDashboardData = ObjectUtils.requireNonNull(data as ProjectDashboardData?)
        viewModel.inputs.configureWith(projectDashboardData)
        binding.creatorDashboardProjectSelector.setOnClickListener {
            projectsListButtonClicked()
        }
        binding.creatorDashboardMessages.setOnClickListener {
            dashboardMessagesButtonClicked()
        }
        binding.creatorViewProjectButton.setOnClickListener {
            viewProjectButtonClicked()
        }
    }

    private fun projectsListButtonClicked() {
        delegate?.projectsListButtonClicked()
    }

    private fun dashboardMessagesButtonClicked() {
        viewModel.inputs.messagesButtonClicked()
    }

    private fun viewProjectButtonClicked() {
        viewModel.inputs.projectButtonClicked()
    }

    private fun setPledgedOfGoalString(currentProject: Project) {
        val pledgedString = ksCurrency.format(currentProject.pledged(), currentProject)
        binding.creatorDashboardAmountRaised.text = pledgedString
        val goalString = ksCurrency.format(currentProject.goal(), currentProject)
        val goalText = ksString.format(context().getString(R.string.discovery_baseball_card_stats_pledged_of_goal), "goal", goalString)
        binding.creatorDashboardFundingText.text = goalText
    }

    private fun setTimeRemainingTextTextView(currentProject: Project) {
        binding.creatorDashboardTimeRemainingText.text = ProjectUtils.deadlineCountdownDetail(currentProject, context(), ksString)
    }

    private fun startMessageThreadsActivity(project: Project, refTag: RefTag) {
        val intent = Intent(context(), MessageThreadsActivity::class.java)
            .putExtra(IntentKey.PROJECT, project)
            .putExtra(IntentKey.REF_TAG, refTag)
        context().startActivity(intent)
    }

    private fun startProjectActivity(project: Project, refTag: RefTag) {
        val intent = Intent(context(), ProjectActivity::class.java)
            .putExtra(IntentKey.PROJECT, project)
            .putExtra(IntentKey.REF_TAG, refTag)
        context().startActivity(intent)
    }
}
