package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment

class ProjectCommentsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProjectCommentsViewModel.ViewModel

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectCommentsViewModel.ViewModel(environment)
    }
}