package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.models.Comment
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.OnCommentCardClickedListener
import com.kickstarter.viewmodels.CommentsViewHolderViewModel

class CommentCardViewHolder(
    val binding: ItemCommentCardBinding,
    val delegate: Delegate
) : KSViewHolder(binding.root) {

    interface Delegate {
        fun onRetryViewClicked(comment: Comment)
        fun onReplyButtonClicked(comment: Comment)
        fun onFlagButtonClicked(comment: Comment)
        fun onCommentGuideLinesClicked(comment: Comment)
        fun onCommentRepliesClicked(comment: Comment)
    }

    private val vm: CommentsViewHolderViewModel.ViewModel = CommentsViewHolderViewModel.ViewModel(environment())
    private val ksString = environment().ksString()

    init {

        this.vm.outputs.commentAuthorName()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.commentsCardView.setCommentUserName(it) }

        this.vm.outputs.commentRepliesCount()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.commentsCardView.setCommentReplies(it) }

        this.vm.outputs.commentAuthorAvatarUrl()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.commentsCardView.setAvatarUrl(it) }

        this.vm.outputs.commentMessageBody()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.commentsCardView.setCommentBody(it) }

        this.vm.outputs.commentCardStatus()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.commentsCardView.setCommentCardStatus(it) }

        this.vm.outputs.isReplyButtonVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.commentsCardView.setReplyButtonVisibility(it) }

        this.vm.outputs.commentPostTime()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.commentsCardView.setCommentPostTime(DateTimeUtils.relative(context(), ksString, it)) }

        this.vm.outputs.isCommentEnableThreads()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.commentsCardView.setCommentEnabledThreads(it) }

        this.vm.outputs.openCommentGuideLines()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this.delegate.onCommentGuideLinesClicked(it) }

        this.vm.outputs.replyToComment()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this.delegate.onReplyButtonClicked(it) }

        this.vm.outputs.retrySendComment()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this.delegate.onRetryViewClicked(it) }

        this.vm.outputs.viewCommentReplies()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this.delegate.onCommentRepliesClicked(it) }

        this.vm.outputs.flagComment()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this.delegate.onFlagButtonClicked(it) }

        this.vm.outputs.newCommentBind()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { vm.inputs.postNewComment(it) }

        binding.commentsCardView.setCommentCardClickedListener(object : OnCommentCardClickedListener {
            override fun onRetryViewClicked(view: View) {
                vm.inputs.onRetryViewClicked()
            }

            override fun onReplyButtonClicked(view: View) {
                vm.inputs.onReplyButtonClicked()
            }

            override fun onFlagButtonClicked(view: View) {
                vm.inputs.onFlagButtonClicked()
            }

            override fun onViewRepliesButtonClicked(view: View) {
                vm.inputs.onViewRepliesButtonClicked()
            }

            override fun onCommentGuideLinesClicked(view: View) {
                vm.inputs.onCommentGuideLinesClicked()
            }
        })
    }

    override fun bindData(data: Any?) {
        this.vm.inputs.configureWith(data as CommentCardData)
    }
}
