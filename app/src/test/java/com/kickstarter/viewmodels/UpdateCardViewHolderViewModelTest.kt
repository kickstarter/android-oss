package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UpdateFactory
import com.kickstarter.models.Update
import org.joda.time.DateTime
import org.junit.Test
import rx.observers.TestSubscriber

class UpdateCardViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: UpdateCardViewHolderViewModel.ViewModel

    private val backersOnlyContainerIsVisible = TestSubscriber.create<Boolean>()
    private val blurb = TestSubscriber.create<String>()
    private val commentsCount = TestSubscriber.create<Int>()
    private val commentsCountIsGone = TestSubscriber.create<Boolean>()
    private val likesCount = TestSubscriber.create<Int>()
    private val likesCountIsGone = TestSubscriber.create<Boolean>()
    private val publishDate = TestSubscriber.create<DateTime>()
    private val sequence = TestSubscriber.create<Int>()
    private val showUpdateDetails = TestSubscriber.create<Update>()
    private val title = TestSubscriber.create<String>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = UpdateCardViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.backersOnlyContainerIsVisible().subscribe(this.backersOnlyContainerIsVisible)
        this.vm.outputs.blurb().subscribe(this.blurb)
        this.vm.outputs.commentsCount().subscribe(this.commentsCount)
        this.vm.outputs.commentsCountIsGone().subscribe(this.commentsCountIsGone)
        this.vm.outputs.likesCount().subscribe(this.likesCount)
        this.vm.outputs.likesCountIsGone().subscribe(this.likesCountIsGone)
        this.vm.outputs.publishDate().subscribe(this.publishDate)
        this.vm.outputs.sequence().subscribe(this.sequence)
        this.vm.outputs.showUpdateDetails().subscribe(this.showUpdateDetails)
        this.vm.outputs.title().subscribe(this.title)
    }

    @Test
    fun testBackersOnlyContainerIsVisible_whenUpdateIsPublic() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectFactory.project(), UpdateFactory.update())

        this.backersOnlyContainerIsVisible.assertValue(false)
    }

    @Test
    fun testBackersOnlyContainerIsVisible_whenUpdateIsNotPublicAndProjectIsNotBacked() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectFactory.project(), UpdateFactory.backersOnlyUpdate())

        this.backersOnlyContainerIsVisible.assertValue(true)
    }

    @Test
    fun testBackersOnlyContainerIsVisible_whenUpdateIsNotPublicAndProjectIsBacked() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectFactory.backedProject(), UpdateFactory.backersOnlyUpdate())

        this.backersOnlyContainerIsVisible.assertValue(false)
    }

    @Test
    fun testBlurb() {
        setUpEnvironment(environment())

        val update = UpdateFactory.update()
            .toBuilder()
            .body("Here are some details.")
            .build()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.blurb.assertValue("Here are some details.")
    }

    @Test
    fun testCommentsCount() {
        setUpEnvironment(environment())

        val update = UpdateFactory.update()
            .toBuilder()
            .commentsCount(33)
            .build()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.commentsCount.assertValue(33)
    }

    @Test
    fun testCommentsCountIsGone_whenCommentsCountIsNull() {
        setUpEnvironment(environment())

        val update = UpdateFactory.update()
            .toBuilder()
            .commentsCount(null)
            .build()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.commentsCountIsGone.assertValue(true)
    }

    @Test
    fun testCommentsCountIsGone_whenCommentsCountIsZero() {
        setUpEnvironment(environment())

        val update = UpdateFactory.update()
            .toBuilder()
            .commentsCount(0)
            .build()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.commentsCountIsGone.assertValue(true)
    }

    @Test
    fun testCommentsCountIsGone_whenCommentsCountIsNotZero() {
        setUpEnvironment(environment())

        val update = UpdateFactory.update()
            .toBuilder()
            .commentsCount(33)
            .build()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.commentsCountIsGone.assertValue(false)
    }

    @Test
    fun testLikesCount() {
        setUpEnvironment(environment())

        val update = UpdateFactory.update()
            .toBuilder()
            .likesCount(22)
            .build()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.likesCount.assertValue(22)
    }

    @Test
    fun testLikesCountIsGone_whenLikesCountIsNull() {
        setUpEnvironment(environment())

        val update = UpdateFactory.update()
            .toBuilder()
            .likesCount(null)
            .build()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.likesCountIsGone.assertValue(true)
    }

    @Test
    fun testLikesCountIsGone_whenLikesCountIsZero() {
        setUpEnvironment(environment())

        val update = UpdateFactory.update()
            .toBuilder()
            .likesCount(0)
            .build()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.likesCountIsGone.assertValue(true)
    }

    @Test
    fun testLikesCountIsGone_whenLikesCountIsNotZero() {
        setUpEnvironment(environment())

        val update = UpdateFactory.update()
            .toBuilder()
            .likesCount(33)
            .build()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.likesCountIsGone.assertValue(false)
    }

    @Test
    fun testPublishDate() {
        setUpEnvironment(environment())

        val timestamp = DateTime.parse("2020-02-26T17:41:07+00:00")
        val update = UpdateFactory.update()
            .toBuilder()
            .publishedAt(timestamp)
            .build()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.publishDate.assertValue(timestamp)
    }

    @Test
    fun testSequence() {
        setUpEnvironment(environment())

        val update = UpdateFactory.update()
            .toBuilder()
            .sequence(3)
            .build()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.sequence.assertValue(3)
    }

    @Test
    fun testShowUpdateDetails() {
        setUpEnvironment(environment())

        val update = UpdateFactory.update()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.vm.inputs.updateClicked()
        this.showUpdateDetails.assertValue(update)
    }

    @Test
    fun testTitle() {
        setUpEnvironment(environment())

        val update = UpdateFactory.update()
            .toBuilder()
            .title("Wow, big news!")
            .build()
        this.vm.inputs.configureWith(ProjectFactory.project(), update)

        this.title.assertValue("Wow, big news!")
    }
}
