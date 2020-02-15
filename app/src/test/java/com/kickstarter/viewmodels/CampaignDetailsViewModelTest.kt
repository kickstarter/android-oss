package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import org.junit.Test
import rx.observers.TestSubscriber

class CampaignDetailsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: CampaignDetailsViewModel.ViewModel

    private val url = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment, project: Project) {
        this.vm = CampaignDetailsViewModel.ViewModel(environment)

        this.vm.outputs.url().subscribe(this.url)

        // Configure the view model with a project.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))
    }

    @Test
    fun testUrl() {
        val projectUrl = "https://www.kickstarter.com/projects/creator/slug"
        val web = Project.Urls.Web.builder()
                .project(projectUrl)
                .rewards("$projectUrl/rewards")
                .updates("$projectUrl/posts")
                .build()
        val project = ProjectFactory.project()
                .toBuilder()
                .urls(Project.Urls.builder().web(web).build())
                .build()

        setUpEnvironment(environment(), project)

        this.url.assertValue("https://www.kickstarter.com/projects/creator/slug/description")
    }

}
