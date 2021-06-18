package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.CommentEnvelopeFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Comment
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.CommentCardData
import org.joda.time.DateTime
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class ThreadViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ThreadViewModel.ViewModel
    private val getComment = TestSubscriber<Comment>()
    private val focusCompose = TestSubscriber<Boolean>()
    private val onReplies = TestSubscriber<List<CommentCardData>>()

    private val createdAt = DateTime.now()

    private fun setUpEnvironment() {
        setUpEnvironment(environment())
    }

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ThreadViewModel.ViewModel(environment)
        this.vm.getRootComment().subscribe(getComment)
        this.vm.shouldFocusOnCompose().subscribe(focusCompose)
        this.vm.onCommentReplies().subscribe(onReplies)
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

    @Test
    fun testLoadCommentReplies_Successful() {
        val env = environment().toBuilder()
            .apolloClient(object : MockApolloClient() {
                override fun getRepliesForComment(
                    comment: Comment,
                    cursor: String?,
                    pageSize: Int
                ): Observable<CommentEnvelope> {
                    return Observable.just(CommentEnvelopeFactory.repliesCommentsEnvelope(createdAt = createdAt))
                }
            })
            .build()

        setUpEnvironment(env)

        this.vm.intent(Intent().putExtra(IntentKey.COMMENT, CommentFactory.reply(createdAt = createdAt)))

        this.onReplies.assertValueCount(1)
    }
}
