package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.viewmodels.ProjectCommentsViewModel

@RequiresActivityViewModel(ProjectCommentsViewModel.ViewModel::class)
class ProjectCommentsActivity : BaseActivity<ProjectCommentsViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState) }

    override fun onDestroy() { super.onDestroy() }

}