package com.kickstarter.ui.viewholders

import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.ProjectNotificationViewBinding
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.ProjectNotification
import com.kickstarter.viewmodels.ProjectNotificationViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ProjectNotificationViewModel.ViewModel::class)
class ProjectNotificationViewHolder(binding: ProjectNotificationViewBinding) :
    KSViewHolder(binding.root) {
    private val viewModel: ProjectNotificationViewModel.ViewModel = ProjectNotificationViewModel.ViewModel(environment())

    init {
        RxView.clicks(binding.enabledSwitch)
            .map { binding.enabledSwitch.isChecked }
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { viewModel.inputs.enabledSwitchClick(it) }

        viewModel.outputs.projectName()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.projectName.text = it }

        viewModel.outputs.enabledSwitch()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(SwitchCompatUtils.setCheckedWithoutAnimation(binding.enabledSwitch))

        viewModel.outputs.showUnableToSaveProjectNotificationError()
            .map { context().getString(R.string.profile_settings_error) }
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ViewUtils.showToast(context()))
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectNotification = ObjectUtils.requireNonNull(data as ProjectNotification?, ProjectNotification::class.java)
        viewModel.projectNotification(projectNotification)
    }
}
