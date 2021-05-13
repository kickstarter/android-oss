package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.viewmodels.CommentsViewModel

@RequiresActivityViewModel(CommentsViewModel.ViewModel::class)
class CommentsActivity : BaseActivity<CommentsViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState) }

    override fun onDestroy() { super.onDestroy() }
}
