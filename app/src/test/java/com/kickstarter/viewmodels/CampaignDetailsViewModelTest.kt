package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ProjectData
import org.junit.Test
import rx.observers.TestSubscriber

class CampaignDetailsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: CampaignDetailsViewModel.ViewModel

    private val goBackToProject = TestSubscriber<Void>()
    private val pledgeContainerIsVisible = TestSubscriber<Boolean>()
    private val url = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment, projectData: ProjectData) {
        this.vm = CampaignDetailsViewModel.ViewModel(environment)

        this.vm.outputs.goBackToProject().subscribe(this.goBackToProject)
        this.vm.outputs.pledgeContainerIsVisible().subscribe(this.pledgeContainerIsVisible)
        this.vm.outputs.url().subscribe(this.url)

        // Configure the view model with ProjectData.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT_DATA, projectData))
    }

    @Test
    fun testGoBackToProject_whenPledgeActionButtonClicked() {
        setUpEnvironment(environment(), ProjectDataFactory.project(ProjectFactory.project()))

        this.vm.inputs.pledgeActionButtonClicked()
        this.goBackToProject.assertValueCount(1)
    }

    @Test
    fun testPledgeContainerIsVisible_whenProjectIsLiveNotBacked_control() {
        setUpEnvironment(environment(), ProjectDataFactory.project(ProjectFactory.project()))

        this.pledgeContainerIsVisible.assertValue(false)
    }

    @Test
    fun testPledgeContainerIsVisible_whenProjectIsLiveNotBacked_variant1() {
        val environment = environment()
                .toBuilder()
                .optimizely(object : MockExperimentsClientType(){
                    override fun variant(experiment: OptimizelyExperiment.Key, user: User?, refTag: RefTag?): OptimizelyExperiment.Variant {
                        return OptimizelyExperiment.Variant.VARIANT_1
                    }
                })
                .build()
        setUpEnvironment(environment, ProjectDataFactory.project(ProjectFactory.project()))

        this.pledgeContainerIsVisible.assertValue(false)
    }

    @Test
    fun testPledgeContainerIsVisible_whenProjectIsLiveNotBacked_variant2() {
        val environment = environment()
                .toBuilder()
                .optimizely(object : MockExperimentsClientType(){
                    override fun variant(experiment: OptimizelyExperiment.Key, user: User?, refTag: RefTag?): OptimizelyExperiment.Variant {
                        return OptimizelyExperiment.Variant.VARIANT_2
                    }
                })
                .build()
        setUpEnvironment(environment, ProjectDataFactory.project(ProjectFactory.project()))

        this.pledgeContainerIsVisible.assertValue(true)
    }

    @Test
    fun testPledgeContainerIsVisible_whenProjectIsNotLive() {
        setUpEnvironment(environment(), ProjectDataFactory.project(ProjectFactory.successfulProject()))

        this.pledgeContainerIsVisible.assertValue(false)
    }

    @Test
    fun testPledgeContainerIsVisible_whenProjectIsBacked() {
        setUpEnvironment(environment(), ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.pledgeContainerIsVisible.assertValue(false)
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

        setUpEnvironment(environment(), ProjectDataFactory.project(project))

        this.url.assertValue("https://www.kickstarter.com/projects/creator/slug/description")
    }

}
