package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
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
    private val date = TestSubscriber.create<DateTime>()
    private val likesCount = TestSubscriber.create<Int>()
    private val likesCountIsGone = TestSubscriber.create<Boolean>()
    private val sequence = TestSubscriber.create<Int>()
    private val title = TestSubscriber.create<String>()
    private val viewUpdate = TestSubscriber.create<Update>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = UpdateCardViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.backersOnlyContainerIsVisible().subscribe(this.backersOnlyContainerIsVisible)
        this.vm.outputs.blurb().subscribe(this.blurb)
        this.vm.outputs.commentsCount().subscribe(this.commentsCount)
        this.vm.outputs.commentsCountIsGone().subscribe(this.commentsCountIsGone)
        this.vm.outputs.date().subscribe(this.date)
        this.vm.outputs.likesCount().subscribe(this.likesCount)
        this.vm.outputs.likesCountIsGone().subscribe(this.likesCountIsGone)
        this.vm.outputs.sequence().subscribe(this.sequence)
        this.vm.outputs.title().subscribe(this.title)
        this.vm.outputs.viewUpdate().subscribe(this.viewUpdate)
    }

    @Test
    fun testBackersOnlyContainerIsVisible() {
        setUpEnvironment(environment())
    }

    @Test
    fun testBlurb() {
        setUpEnvironment(environment())
    }

    @Test
    fun testCommentsCount() {
        setUpEnvironment(environment())
    }

    @Test
    fun testcommentsCountIsGone() {
        setUpEnvironment(environment())
    }

    @Test
    fun testDate() {
        setUpEnvironment(environment())
    }

    @Test
    fun testLikesCount() {
        setUpEnvironment(environment())
    }

    @Test
    fun testLikesCountIsGoneDigits() {
        setUpEnvironment(environment())
    }

    @Test
    fun testSequence() {
        setUpEnvironment(environment())
    }

    @Test
    fun testTitle() {
        setUpEnvironment(environment())
    }

    @Test
    fun testViewUpdate() {
        setUpEnvironment(environment())
    }

}
