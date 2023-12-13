package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ItemRootCommentCardBinding
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.OnCommentCardClickedListener
import com.kickstarter.viewmodels.RootCommentViewHolderViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class RootCommentViewHolder(
    val binding: ItemRootCommentCardBinding
) : KSViewHolder(binding.root) {

    private val vm: RootCommentViewHolderViewModel.ViewModel =
        RootCommentViewHolderViewModel.ViewModel(environment())
    private val ksString = requireNotNull(environment().ksString())
    private val disposables = CompositeDisposable()

    init {

        binding.commentsCardView.setFlaggedMessage(
            context().getString(R.string.This_comment_is_under_review_for_potentially_violating_kickstarters_community_guidelines)
        )

        this.vm.outputs.bindRootComment()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { commentCardData ->
                CommentCardStatus.values()
                    .firstOrNull { commentCardData.commentCardState == it.commentCardStatus }?.let {
                        if (it == CommentCardStatus.CANCELED_PLEDGE_MESSAGE || it == CommentCardStatus.FLAGGED_COMMENT) {
                            binding.commentsCardView.setCommentCardStatus(it)

                            context().getString(R.string.This_person_canceled_their_pledge)
                                .also { message ->
                                    binding.commentsCardView.setCancelPledgeMessage(message)
                                }

                            binding.commentsCardView.setCommentCardClickedListener(object :
                                    OnCommentCardClickedListener {
                                    override fun onRetryViewClicked(view: View) {
                                    }

                                    override fun onReplyButtonClicked(view: View) {
                                    }

                                    override fun onFlagButtonClicked(view: View) {
                                    }

                                    override fun onViewRepliesButtonClicked(view: View) {
                                    }

                                    override fun onCommentGuideLinesClicked(view: View) {
                                    }

                                    override fun onShowCommentClicked(view: View) {
                                        vm.inputs.onShowCanceledPledgeRootCommentClicked()
                                    }
                                })
                        }
                    }

                commentCardData?.comment?.let { comment ->
                    binding.commentsCardView.setCommentUserName(comment.author().name())
                    binding.commentsCardView.setCommentBody(comment.body())
                    binding.commentsCardView.hideReplyButton()
                    comment.createdAt()?.let { createdAt ->
                        binding.commentsCardView.setCommentPostTime(
                            DateTimeUtils.relative(
                                context(),
                                ksString,
                                createdAt
                            )
                        )
                    }
                    binding.commentsCardView.setCommentUserName(comment.author().name())
                    binding.commentsCardView.setAvatarUrl(comment.author().avatar().medium())
                }
            }.addToDisposable(disposables)

        this.vm.outputs.showCanceledPledgeRootComment()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.commentsCardView.setCommentCardStatus(it)
            }
            .addToDisposable(disposables)

        this.vm.outputs.authorBadge()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.commentsCardView.setCommentBadge(it) }
            .addToDisposable(disposables)
    }

    override fun bindData(data: Any?) {
        if (data is CommentCardData) {
            this.vm.inputs.configureWith(data)
        }
    }

    override fun destroy() {
        disposables.clear()
        vm.clear()
        super.destroy()
    }
}
