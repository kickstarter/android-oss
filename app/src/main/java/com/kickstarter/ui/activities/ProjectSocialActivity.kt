package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.databinding.ProjectSocialLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.ui.adapters.ProjectSocialAdapter
import com.kickstarter.ui.viewholders.ProjectContextViewHolder
import com.kickstarter.viewmodels.ProjectSocialViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(ProjectSocialViewModel.ViewModel::class)
class ProjectSocialActivity : BaseActivity<ProjectSocialViewModel.ViewModel>(), ProjectSocialAdapter.Delegate {

    private lateinit var binding: ProjectSocialLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProjectSocialLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ProjectSocialAdapter(this)
        binding.projectSocialRecyclerView.adapter = adapter
        binding.projectSocialRecyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.outputs.project()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter.takeProject(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.projectSocialRecyclerView.adapter = null
    }

    override fun exitTransition() = TransitionUtils.slideInFromLeft()
    override fun projectContextClicked(viewHolder: ProjectContextViewHolder?) = back()
}
