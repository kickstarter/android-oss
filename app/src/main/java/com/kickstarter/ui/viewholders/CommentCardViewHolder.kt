package com.kickstarter.ui.viewholders

import android.view.View
import androidx.constraintlayout.widget.Constraints
import com.kickstarter.R
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.models.Comment
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.OnCommentCardClickedListener
import com.kickstarter.viewmodels.CommentsViewHolderViewModel

class CommentCardViewHolder(
    val binding: ItemCommentCardBinding,
    val delegate: Delegate,
    val isReply: Boolean = false
) : KSViewHolder(binding.root) {

    interface Delegate {
        fun onRetryViewClicked(comment: Comment)
        fun onReplyButtonClicked(comment: Comment)
        fun onFlagButtonClicked(comment: Comment)
        fun onCommentGuideLinesClicked(comment: Comment)
        fun onCommentRepliesClicked(comment: Comment)
        fun onCommentPostedSuccessFully(comment: Comment)
        fun onCommentPostedFailed(comment: Comment)
        fun onShowCommentClicked(comment: Comment)
    }

    private val vm: CommentsViewHolderViewModel.ViewModel = CommentsViewHolderViewModel.ViewModel(environment())
    private val ksString = environment().ksString()

    init {

        this.vm.outputs.isCommentReply()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.commentsCardView.setSeparatorVisibility(false) }

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

        this.vm.outputs.showCanceledComment()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this.delegate.onShowCommentClicked(it) }

        this.vm.outputs.isSuccessfullyPosted()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this.delegate.onCommentPostedSuccessFully(it) }

        this.vm.outputs.isFailedToPost()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this.delegate.onCommentPostedFailed(it) }

        this.vm.outputs.authorBadge()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.commentsCardView.setCommentBadge(it) }

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

            override fun onShowCommentClicked(view: View) {
                vm.inputs.onShowCommentClicked()
            }
        })

        binding.commentsCardView.setFlaggedMessage(
            context().getString(R.string.This_comment_has_been_removed_by_Kickstarter) +
                context().getString(R.string.Learn_more_about_comment_guidelines)
        )

        binding.commentsCardView.setCancelPledgeMessage(
            context().getString(R.string.this_person_canceled_pledge) +
                context().getString(R.string.show_comment)
        )

        if (isReply) {
            val params = Constraints.LayoutParams(
                Constraints.LayoutParams.MATCH_PARENT,
                Constraints.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(context().resources.getDimension(R.dimen.grid_5).toInt(), 0, 0, 0)
            binding.cardCl.layoutParams = params
        }
    }

    override fun bindData(data: Any?) {
        this.vm.inputs.configureWith(data as CommentCardData)
    }
}
