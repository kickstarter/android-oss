package com.kickstarter.ui.viewholders

import android.view.View
import androidx.constraintlayout.widget.Constraints
import com.kickstarter.R
import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Comment
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.OnCommentCardClickedListener
import com.kickstarter.viewmodels.CommentsViewHolderViewModel
import io.reactivex.disposables.CompositeDisposable

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
        fun onCommentPostedSuccessFully(comment: Comment, position: Int)
        fun onCommentPostedFailed(comment: Comment, position: Int)
        fun onShowCommentClicked(comment: Comment)
    }

    private val vm: CommentsViewHolderViewModel.ViewModel = CommentsViewHolderViewModel.ViewModel(environment())
    private val ksString = requireNotNull(environment().ksString())
    private val disposables = CompositeDisposable()

    init {

        this.vm.outputs.isCommentReply()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.commentsCardView.setSeparatorVisibility(false) }
            .addToDisposable(disposables)

        this.vm.outputs.commentAuthorName()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.commentsCardView.setCommentUserName(it) }
            .addToDisposable(disposables)

        this.vm.outputs.commentRepliesCount()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.commentsCardView.setCommentReplies(it) }
            .addToDisposable(disposables)

        this.vm.outputs.commentAuthorAvatarUrl()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.commentsCardView.setAvatarUrl(it) }
            .addToDisposable(disposables)

        this.vm.outputs.commentMessageBody()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.commentsCardView.setCommentBody(it) }
            .addToDisposable(disposables)

        this.vm.outputs.commentCardStatus()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.commentsCardView.setCommentCardStatus(it) }
            .addToDisposable(disposables)

        this.vm.outputs.isReplyButtonVisible()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.commentsCardView.setReplyButtonVisibility(it) }
            .addToDisposable(disposables)

        this.vm.outputs.commentPostTime()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.commentsCardView.setCommentPostTime(DateTimeUtils.relative(context(), ksString, it)) }
            .addToDisposable(disposables)

        this.vm.outputs.isCommentEnableThreads()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.commentsCardView.setCommentEnabledThreads(it) }
            .addToDisposable(disposables)

        this.vm.outputs.openCommentGuideLines()
            .compose(Transformers.observeForUIV2())
            .subscribe { this.delegate.onCommentGuideLinesClicked(it) }
            .addToDisposable(disposables)

        this.vm.outputs.replyToComment()
            .compose(Transformers.observeForUIV2())
            .subscribe { this.delegate.onReplyButtonClicked(it) }
            .addToDisposable(disposables)

        this.vm.outputs.retrySendComment()
            .compose(Transformers.observeForUIV2())
            .subscribe { this.delegate.onRetryViewClicked(it) }
            .addToDisposable(disposables)

        this.vm.outputs.viewCommentReplies()
            .compose(Transformers.observeForUIV2())
            .subscribe { this.delegate.onCommentRepliesClicked(it) }
            .addToDisposable(disposables)

        this.vm.outputs.flagComment()
            .compose(Transformers.observeForUIV2())
            .subscribe { this.delegate.onFlagButtonClicked(it) }
            .addToDisposable(disposables)

        this.vm.outputs.showCanceledComment()
            .compose(Transformers.observeForUIV2())
            .subscribe { this.delegate.onShowCommentClicked(it) }
            .addToDisposable(disposables)

        this.vm.outputs.isSuccessfullyPosted()
            .compose(Transformers.observeForUIV2())
            .subscribe { this.delegate.onCommentPostedSuccessFully(it, absoluteAdapterPosition) }
            .addToDisposable(disposables)

        this.vm.outputs.isFailedToPost()
            .compose(Transformers.observeForUIV2())
            .subscribe { this.delegate.onCommentPostedFailed(it, absoluteAdapterPosition) }
            .addToDisposable(disposables)

        this.vm.outputs.authorBadge()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.commentsCardView.setCommentBadge(it) }
            .addToDisposable(disposables)

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
            context().getString(R.string.This_comment_is_under_review_for_potentially_violating_kickstarters_community_guidelines)
        )

        binding.commentsCardView.setRemovedMessage(
            context().getString(R.string.This_comment_has_been_removed_by_Kickstarter) +
                context().getString(R.string.Learn_more_about_comment_guidelines)
        )

        context().getString(R.string.This_person_canceled_their_pledge).also {
            binding.commentsCardView.setCancelPledgeMessage(it)
        }

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

    override fun destroy() {
        disposables.clear()
        vm.onCleared()
        super.destroy()
    }
}
