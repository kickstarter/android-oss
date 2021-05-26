package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.models.Comment
import com.kickstarter.ui.IntentKey
import org.junit.Test
import rx.observers.TestSubscriber

class ThreadViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ThreadViewModel.ViewModel
    private val getComment = TestSubscriber<Comment>()
    private val focusCompose = TestSubscriber<Boolean>()

    private fun setUpEnvironment() {
        setUpEnvironment(environment())
    }

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ThreadViewModel.ViewModel(environment)
        this.vm.getRootComment().subscribe(getComment)
        this.vm.shouldFocusOnCompose().subscribe(focusCompose)
    }

    @Test
    fun testGetRootComment() {
        setUpEnvironment()

        val comment = CommentFactory.comment(avatar = AvatarFactory.avatar())

        this.vm.intent(Intent().putExtra(IntentKey.COMMENT, comment))
        getComment.assertValue(comment)

        this.vm.intent(Intent().putExtra("Some other Key", comment))
        getComment.assertValue(comment)
    }

    @Test
    fun testShouldFocusCompose() {
        setUpEnvironment()

        this.vm.intent(Intent().putExtra(IntentKey.REPLY_EXPAND, false))
        focusCompose.assertValue(false)

        this.vm.intent(Intent().putExtra("Some other Key", false))
        focusCompose.assertValues(false)

        this.vm.intent(Intent().putExtra(IntentKey.REPLY_EXPAND, true))
        focusCompose.assertValues(false, true)
    }
}
