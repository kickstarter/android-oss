package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.models.Comment
import org.junit.Test
import rx.observers.TestSubscriber

class RootCommentViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: RootCommentViewHolderViewModel.ViewModel

    private val bindRootComment = TestSubscriber<Comment>()

    private fun setupEnvironment() {
        this.vm = RootCommentViewHolderViewModel.ViewModel(environment())
        this.vm.outputs.bindRootComment().subscribe(bindRootComment)
    }

    @Test
    fun bindRootCommentTest() {
        setupEnvironment()
        val comment = CommentFactory.comment()
        this.vm.inputs.configureWith(comment)
        this.bindRootComment.assertValue(comment)
    }
}
