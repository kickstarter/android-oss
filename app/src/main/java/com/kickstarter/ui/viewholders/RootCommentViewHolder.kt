package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ItemRootCommentCardBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.OnCommentCardClickedListener
import com.kickstarter.viewmodels.RootCommentViewHolderViewModel

@Suppress("UNCHECKED_CAST")
class RootCommentViewHolder(
    val binding: ItemRootCommentCardBinding
) : KSViewHolder(binding.root) {

    private val vm: RootCommentViewHolderViewModel.ViewModel = RootCommentViewHolderViewModel.ViewModel(environment())
    private val ksString = environment().ksString()
    init {
        this.vm.outputs.bindRootComment()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { commentCardData ->
                CommentCardStatus.values().firstOrNull { commentCardData.commentCardState == it.commentCardStatus }?.let {
                    if (it == CommentCardStatus.CANCELED_PLEDGE_MESSAGE) {
                        binding.commentsCardView.setCommentCardStatus(it)
                        binding.commentsCardView.setCancelPledgeMessage(
                            context().getString(R.string.this_person_canceled_pledge).plus(" ").plus(context().getString(R.string.show_comment))
                        )
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
                    binding.commentsCardView.setCommentPostTime(DateTimeUtils.relative(context(), ksString, comment.createdAt()))
                    binding.commentsCardView.setCommentUserName(comment.author().name())
                    binding.commentsCardView.setAvatarUrl(comment.author().avatar().medium())
                }
            }

        this.vm.outputs.showCanceledPledgeRootComment()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding.commentsCardView.setCommentCardStatus(it)
            }

        this.vm.outputs.authorBadge()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.commentsCardView.setCommentBadge(it) }
    }

    override fun bindData(data: Any?) {
        if (data is CommentCardData) {
            this.vm.inputs.configureWith(data)
        }
    }
}
