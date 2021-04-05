package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.CreatorDashboardProjectSwitcherViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.viewmodels.CreatorDashboardBottomSheetHolderViewModel

class CreatorDashboardBottomSheetViewHolder(
    private val binding: CreatorDashboardProjectSwitcherViewBinding,
    delegate: Delegate
) : KSViewHolder(binding.root) {
    private val viewModel = CreatorDashboardBottomSheetHolderViewModel.ViewModel(environment())

    interface Delegate {
        fun projectSelectionInput(project: Project?)
    }

    private fun projectSwitcherProjectClicked() {
        viewModel.inputs.projectSwitcherProjectClicked()
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val project = ObjectUtils.requireNonNull(data as Project?)
        viewModel.inputs.projectInput(project)
        binding.creatorDashboardBottomSheetProjectView.setOnClickListener {
            projectSwitcherProjectClicked()
        }
    }

    init {
        viewModel.outputs.projectNameText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.creatorDashboardProjectSwitcherProjectTitle.text = it }
        viewModel.outputs.projectLaunchDate()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.creatorDashboardProjectSwitcherProjectLaunch.text = DateTimeUtils.longDate(it) }
        viewModel.outputs.projectSwitcherProject()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { delegate.projectSelectionInput(it) }
    }
}
