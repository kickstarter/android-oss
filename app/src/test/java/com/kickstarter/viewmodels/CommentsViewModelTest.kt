package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment

class CommentsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: CommentsViewModel.ViewModel

    private fun setUpEnvironment(environment: Environment) {
        this.vm = CommentsViewModel.ViewModel(environment)
    }
}
