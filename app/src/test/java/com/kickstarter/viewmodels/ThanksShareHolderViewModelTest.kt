package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.CheckoutDataFactory
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UserFactory.creator
import com.kickstarter.models.Project
import com.kickstarter.models.Urls
import com.kickstarter.models.Web
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class ThanksShareHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ThanksShareHolderViewModel.ThanksShareViewHolderViewModel

    private val projectName = TestSubscriber<String>()
    private val startShare = TestSubscriber<Pair<String, String>>()
    private val startShareOnFacebook = TestSubscriber<Pair<Project, String>>()
    private val startShareOnTwitter = TestSubscriber<Pair<String, String>>()
    private val postCampaignText = TestSubscriber<Pair<Double, Project>>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment() {
        vm = ThanksShareHolderViewModel.ThanksShareViewHolderViewModel(environment())
        vm.outputs.projectName().subscribe { projectName.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startShare().subscribe { startShare.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startShareOnFacebook().subscribe { startShareOnFacebook.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.startShareOnTwitter().subscribe { startShareOnTwitter.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.postCampaignPledgeText().subscribe { postCampaignText.onNext(it) }
            .addToDisposable(disposables)
    }

    @Test
    fun testProjectName() {
        val project = project()
        setUpEnvironment()

        val checkoutData = CheckoutDataFactory.checkoutData(
            3L,
            20.0,
            30.0
        )
        vm.configureWith(Pair(project, checkoutData))

        projectName.assertValues(project.name())
    }

    @Test
    fun testStartShare() {
        setUpEnvironment()

        val project = setUpProjectWithWebUrls()
        val checkoutData = CheckoutDataFactory.checkoutData(
            3L,
            20.0,
            30.0
        )
        vm.configureWith(Pair(project, checkoutData))
        vm.inputs.shareClick()
        val expectedShareUrl =
            "https://www.kck.str/projects/15/best-project-2k19?ref=android_thanks_share"

        startShare.assertValue(Pair.create("Best Project 2K19", expectedShareUrl))
    }

    @Test
    fun testStartShareOnFacebook() {
        setUpEnvironment()

        val project = setUpProjectWithWebUrls()
        val checkoutData = CheckoutDataFactory.checkoutData(
            3L,
            20.0,
            30.0
        )

        vm.configureWith(Pair(project, checkoutData))

        vm.inputs.shareOnFacebookClick()
        val expectedShareUrl =
            "https://www.kck.str/projects/15/best-project-2k19?ref=android_thanks_facebook_share"

        startShareOnFacebook.assertValue(Pair.create(project, expectedShareUrl))
    }

    @Test
    fun testStartShareOnTwitter() {
        setUpEnvironment()

        val project = setUpProjectWithWebUrls()
        val checkoutData = CheckoutDataFactory.checkoutData(
            3L,
            20.0,
            30.0
        )
        vm.configureWith(Pair(project, checkoutData))
        vm.inputs.shareOnTwitterClick()
        val expectedShareUrl =
            "https://www.kck.str/projects/15/best-project-2k19?ref=android_thanks_twitter_share"

        startShareOnTwitter.assertValue(Pair.create("Best Project 2K19", expectedShareUrl))
    }

    @Test
    fun testShowLatePledges_whenTrue_ShowLatePledgeFlow() {
        setUpEnvironment()

        val project = setUpProjectWithWebUrls().toBuilder().isInPostCampaignPledgingPhase(true).postCampaignPledgingEnabled(true).build()
        val checkoutData = CheckoutDataFactory.checkoutData(
            3L,
            20.0,
            30.0
        )
        vm.configureWith(Pair(project, checkoutData))

        postCampaignText.assertValue(Pair(checkoutData.amount(), project))
        projectName.assertNoValues()
    }

    @Test
    fun testShowLatePledges_whenFalse_noEmission() {
        setUpEnvironment()

        val project = setUpProjectWithWebUrls().toBuilder().isInPostCampaignPledgingPhase(false).postCampaignPledgingEnabled(false).build()
        val checkoutData = CheckoutDataFactory.checkoutData(
            3L,
            20.0,
            30.0
        )
        vm.configureWith(Pair(project, checkoutData))

        postCampaignText.assertNoValues()
        projectName.assertValue(project.name())
    }

    @Test
    fun testShowLatePledges_whenNull_noEmission() {
        setUpEnvironment()

        val project = setUpProjectWithWebUrls().toBuilder().isInPostCampaignPledgingPhase(null).postCampaignPledgingEnabled(null).build()
        val checkoutData = CheckoutDataFactory.checkoutData(
            3L,
            20.0,
            30.0
        )
        vm.configureWith(Pair(project, checkoutData))

        postCampaignText.assertNoValues()
        projectName.assertValue(project.name())
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

    @After
    fun clear() {
        vm.inputs.onCleared()
        disposables.clear()
    }
}
