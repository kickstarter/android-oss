package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import org.junit.Test
import rx.observers.TestSubscriber

class RewardFragmentViewModelTest: KSRobolectricTestCase() {

    private lateinit var vm: RewardFragmentViewModel.ViewModel
    private val project = TestSubscriber.create<Project>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = RewardFragmentViewModel.ViewModel(environment)
        this.vm.outputs.project().subscribe(this.project)
    }

    @Test
    fun testProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.project(project)
        this.project.assertValue(project)
    }
}
