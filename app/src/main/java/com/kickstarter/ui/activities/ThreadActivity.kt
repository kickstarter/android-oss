package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.core.view.isVisible
import com.kickstarter.databinding.ActivityThreadLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.models.Comment
import com.kickstarter.ui.adapters.RepliesAdapter
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.views.OnCommentComposerViewClickedListener
import com.kickstarter.viewmodels.ThreadViewModel
import org.joda.time.DateTime
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

@RequiresActivityViewModel(ThreadViewModel.ViewModel::class)
class ThreadActivity :
    BaseActivity<ThreadViewModel.ViewModel>(),
    RepliesAdapter.Delegate {

    private lateinit var binding: ActivityThreadLayoutBinding
    private lateinit var ksString: KSString

    private val adapter = RepliesAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThreadLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ksString = environment().ksString()

        binding.commentRepliesRecyclerView.adapter = adapter

        this.viewModel.getRootComment()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { comment ->
                configureRootCommentView(comment)
            }

        this.viewModel.onCommentReplies()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { comments ->
                this.adapter.takeData(comments)
            }

        this.viewModel.shouldFocusOnCompose()
            .delay(30, TimeUnit.MILLISECONDS)
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { shouldOpenKeyboard ->
                binding.replyComposer.requestCommentComposerKeyBoard(shouldOpenKeyboard)
            }

        viewModel.outputs.currentUserAvatar()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.replyComposer.setAvatarUrl(it)
            }

        viewModel.outputs.replyComposerStatus()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.replyComposer.setCommentComposerStatus(it)
            }

        viewModel.outputs.scrollToBottom()
            .compose(bindToLifecycle())
            .delay(200, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.commentRepliesRecyclerView.scrollToPosition(5)
            }

        viewModel.outputs.showReplyComposer()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.replyComposer.isVisible = it
            }

        binding.replyComposer.setCommentComposerActionClickListener(object :
                OnCommentComposerViewClickedListener {
                override fun onClickActionListener(string: String) {
                    postReply(string)
                    hideKeyboard()
                }
            })
    }

    fun postReply(comment: String) {
        this.viewModel.inputs.insertNewReplyToList(comment, DateTime.now())
        this.binding.replyComposer.clearCommentComposer()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    private fun configureRootCommentView(comment: Comment) {
        binding.commentsCardView.setCommentUserName(comment.author().name())
        binding.commentsCardView.setCommentBody(comment.body())
        binding.commentsCardView.hideReplyButton()
        binding.commentsCardView.setCommentPostTime(DateTimeUtils.relative(this, ksString, comment.createdAt()))
        binding.commentsCardView.setCommentUserName(comment.author().name())
        binding.commentsCardView.setAvatarUrl(comment.author().avatar().medium())
    }

    override fun onRetryViewClicked(comment: Comment) {
        TODO("Not yet implemented")
    }

    override fun onReplyButtonClicked(comment: Comment) {
        TODO("Not yet implemented")
    }

    override fun onFlagButtonClicked(comment: Comment) {
        TODO("Not yet implemented")
    }

    override fun onCommentGuideLinesClicked(comment: Comment) {
        TODO("Not yet implemented")
    }

    override fun onCommentRepliesClicked(comment: Comment) {
        TODO("Not yet implemented")
    }

    override fun onCommentPostedSuccessFully(comment: Comment) {

    }
}
