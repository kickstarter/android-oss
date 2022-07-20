package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UserFactory.creator
import com.kickstarter.models.Project
import com.kickstarter.models.Urls
import com.kickstarter.models.Web
import org.junit.Test
import rx.observers.TestSubscriber

class ThanksShareHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ThanksShareHolderViewModel.ViewModel

    private val projectName = TestSubscriber<String>()
    private val startShare = TestSubscriber<Pair<String, String>>()
    private val startShareOnFacebook = TestSubscriber<Pair<Project, String>>()
    private val startShareOnTwitter = TestSubscriber<Pair<String, String>>()

    protected fun setUpEnvironment(environment: Environment) {
        vm = ThanksShareHolderViewModel.ViewModel(environment)
        vm.outputs.projectName().subscribe(projectName)
        vm.outputs.startShare().subscribe(startShare)
        vm.outputs.startShareOnFacebook().subscribe(startShareOnFacebook)
        vm.outputs.startShareOnTwitter().subscribe(startShareOnTwitter)
    }

    @Test
    fun testProjectName() {
        val project = project()
        setUpEnvironment(environment())

        vm.configureWith(project)

        projectName.assertValues(project.name())
    }

    @Test
    fun testStartShare() {
        setUpEnvironment(environment())

        val project = setUpProjectWithWebUrls()

        vm.configureWith(project)
        vm.inputs.shareClick()
        val expectedShareUrl =
            "https://www.kck.str/projects/15/best-project-2k19?ref=android_thanks_share"

        startShare.assertValue(Pair.create("Best Project 2K19", expectedShareUrl))
    }

    @Test
    fun testStartShareOnFacebook() {
        setUpEnvironment(environment())

        val project = setUpProjectWithWebUrls()
        vm.configureWith(project)
        vm.inputs.shareOnFacebookClick()
        val expectedShareUrl =
            "https://www.kck.str/projects/15/best-project-2k19?ref=android_thanks_facebook_share"

        startShareOnFacebook.assertValue(Pair.create(project, expectedShareUrl))
    }

    @Test
    fun testStartShareOnTwitter() {
        setUpEnvironment(environment())

        val project = setUpProjectWithWebUrls()
        vm.configureWith(project)
        vm.inputs.shareOnTwitterClick()
        val expectedShareUrl =
            "https://www.kck.str/projects/15/best-project-2k19?ref=android_thanks_twitter_share"

        startShareOnTwitter.assertValue(Pair.create("Best Project 2K19", expectedShareUrl))
    }

    private fun setUpProjectWithWebUrls(): Project {
        val creatorId = 15L
        val creator = creator()
            .toBuilder()
            .id(creatorId)
            .build()
        val slug = "best-project-2k19"
        val projectUrl = "https://www.kck.str/projects/" + creator.id() + "/" + slug
        val webUrls = Web.builder()
            .project(projectUrl)
            .rewards("\$projectUrl/rewards")
            .updates("\$projectUrl/posts")
            .build()
        return project()
            .toBuilder()
            .name("Best Project 2K19")
            .urls(Urls.builder().web(webUrls).build())
            .build()
    }
}
