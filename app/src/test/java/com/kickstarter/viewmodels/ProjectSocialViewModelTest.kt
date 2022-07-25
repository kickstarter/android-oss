package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import org.junit.Test
import rx.observers.TestSubscriber

class ProjectSocialViewModelTest : KSRobolectricTestCase() {
    private val project = TestSubscriber<Project>()

    @Test
    fun testProjectInit() {
        val project = ProjectFactory.project()

        val intent = Intent().apply {
            putExtra(IntentKey.PROJECT, project)
        }

        val vm = ProjectSocialViewModel.ViewModel(environment())
            .also {
                it.intent(intent)
            }

        vm.outputs.project().subscribe(this.project)

        this.project.assertValue(project)
    }
}
