package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UserFactory.creator
import com.kickstarter.models.Project
import com.kickstarter.models.Urls
import com.kickstarter.models.Web
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class ThanksShareHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ThanksShareHolderViewModel.ThanksShareViewHolderViewModel

    private val projectName = TestSubscriber<String>()
    private val startShare = TestSubscriber<Pair<String, String>>()
    private val startShareOnFacebook = TestSubscriber<Pair<Project, String>>()
    private val startShareOnTwitter = TestSubscriber<Pair<String, String>>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment() {
        vm = ThanksShareHolderViewModel.ThanksShareViewHolderViewModel()
        vm.outputs.projectName().subscribe { projectName.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startShare().subscribe { startShare.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startShareOnFacebook().subscribe { startShareOnFacebook.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.startShareOnTwitter().subscribe { startShareOnTwitter.onNext(it) }
            .addToDisposable(disposables)
    }

    @Test
    fun testProjectName() {
        val project = project()
        setUpEnvironment()

        vm.configureWith(project)

        projectName.assertValues(project.name())
    }

    @Test
    fun testStartShare() {
        setUpEnvironment()

        val project = setUpProjectWithWebUrls()

        vm.configureWith(project)
        vm.inputs.shareClick()
        val expectedShareUrl =
            "https://www.kck.str/projects/15/best-project-2k19?ref=android_thanks_share"

        startShare.assertValue(Pair.create("Best Project 2K19", expectedShareUrl))
    }

    @Test
    fun testStartShareOnFacebook() {
        setUpEnvironment()

        val project = setUpProjectWithWebUrls()
        vm.configureWith(project)
        vm.inputs.shareOnFacebookClick()
        val expectedShareUrl =
            "https://www.kck.str/projects/15/best-project-2k19?ref=android_thanks_facebook_share"

        startShareOnFacebook.assertValue(Pair.create(project, expectedShareUrl))
    }

    @Test
    fun testStartShareOnTwitter() {
        setUpEnvironment()

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
