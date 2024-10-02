package com.kickstarter.ui.viewholders

import com.kickstarter.R
import com.kickstarter.databinding.ProjectNotificationViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.ProjectNotification
import com.kickstarter.viewmodels.ProjectNotificationViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ProjectNotificationViewHolder(binding: ProjectNotificationViewBinding) :
    KSViewHolder(binding.root) {
    private val viewModel: ProjectNotificationViewModel.ViewModel = ProjectNotificationViewModel.ViewModel(environment())
    private val disposables = CompositeDisposable()

    init {
        binding.enabledSwitch.setOnClickListener {
            viewModel.inputs.enabledSwitchClick(binding.enabledSwitch.isChecked)
        }

        viewModel.outputs.projectName()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.projectName.text = it }
            .addToDisposable(disposables)

        viewModel.outputs.enabledSwitch()
            .compose(Transformers.observeForUIV2())
            .subscribe { SwitchCompatUtils.setCheckedWithoutAnimation(binding.enabledSwitch) }
            .addToDisposable(disposables)

        viewModel.outputs.showUnableToSaveProjectNotificationError()
            .map { context().getString(R.string.profile_settings_error) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { ViewUtils.showToast(context()) }
            .addToDisposable(disposables)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectNotification = requireNotNull(data as ProjectNotification?) { ProjectNotification::class.java.toString() + " required to be non-null." }
        viewModel.projectNotification(projectNotification)
    }

    override fun destroy() {
        disposables.clear()
        super.destroy()
    }
}
