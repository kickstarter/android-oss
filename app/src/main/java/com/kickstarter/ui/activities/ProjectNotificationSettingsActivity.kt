package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ProjectNotificationSettingsLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.ui.adapters.ProjectNotificationSettingsAdapter
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.ProjectNotificationSettingsViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ProjectNotificationSettingsViewModel.ViewModel::class)
class ProjectNotificationSettingsActivity : BaseActivity<ProjectNotificationSettingsViewModel.ViewModel>() {
    private lateinit var binding: ProjectNotificationSettingsLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProjectNotificationSettingsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ProjectNotificationSettingsAdapter()

        binding.projectNotificationSettingsRecyclerView.adapter = adapter
        binding.projectNotificationSettingsRecyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.outputs.projectNotifications()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.projectNotifications(it) }

        viewModel.outputs.unableToFetchProjectNotificationsError()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showSnackbar(
                    binding.projectNotificationSettingsRecyclerView,
                    getString(R.string.general_error_something_wrong)
                )
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.projectNotificationSettingsRecyclerView.adapter = null
    }
}
