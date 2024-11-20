package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ProjectNotificationSettingsLayoutBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.adapters.ProjectNotificationSettingsAdapter
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.ProjectNotificationSettingsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ProjectNotificationSettingsActivity : ComponentActivity() {
    private lateinit var binding: ProjectNotificationSettingsLayoutBinding
    private lateinit var viewModelFactory: ProjectNotificationSettingsViewModel.Factory
    private val viewModel: ProjectNotificationSettingsViewModel.ProjectNotificationSettingsViewModel by viewModels {
        viewModelFactory
    }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getEnvironment()?.let { env ->
            viewModelFactory = ProjectNotificationSettingsViewModel.Factory(env)
        }
        binding = ProjectNotificationSettingsLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root,

        )
        setContentView(binding.root)

        val adapter = ProjectNotificationSettingsAdapter()

        binding.projectNotificationSettingsRecyclerView.adapter = adapter
        binding.projectNotificationSettingsRecyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.outputs.projectNotifications()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.projectNotifications(it) }
            .addToDisposable(disposables)

        viewModel.outputs.unableToFetchProjectNotificationsError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showSnackbar(
                    binding.projectNotificationSettingsRecyclerView,
                    getString(R.string.general_error_something_wrong)
                )
            }
            .addToDisposable(disposables)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        binding.projectNotificationSettingsRecyclerView.adapter = null
    }
}
