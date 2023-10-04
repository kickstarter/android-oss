package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ProjectData
import io.reactivex.subscribers.TestSubscriber
import com.kickstarter.viewmodels.ProjectSocialViewModel.Factory
import com.kickstarter.viewmodels.ProjectSocialViewModel.ProjectSocialViewModel
import com.kickstarter.viewmodels.projectpage.ProjectOverviewViewModel
import io.reactivex.disposables.CompositeDisposable
import org.junit.After
import org.junit.Test

class ProjectSocialViewModelTest : KSRobolectricTestCase() {
    private val project = TestSubscriber<Project>()
    private val disposables = CompositeDisposable()
    private lateinit var vm: ProjectSocialViewModel
    @After
    fun cleanUp() {
        disposables.clear()
    }

    private fun setUpEnvironment(environment: Environment = environment(), intent: Intent) {
        vm = Factory (environment, intent).create(ProjectSocialViewModel::class.java)
        vm.outputs.project().subscribe{this.project.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testProjectInit() {
        val project = ProjectFactory.project()
            .toBuilder()
            .friends(listOf(UserFactory.user(), UserFactory.canadianUser(), UserFactory.socialUser()))
            .build()

        val intent = Intent().apply {
            putExtra(IntentKey.PROJECT, project)
        }

        setUpEnvironment(intent = intent)

        this.project.assertValue(project)
    }
}
