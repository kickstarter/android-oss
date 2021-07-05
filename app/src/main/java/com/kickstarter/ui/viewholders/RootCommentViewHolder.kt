package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ItemCommentCardBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.models.Comment
import com.kickstarter.viewmodels.RootCommentViewHolderViewModel

@Suppress("UNCHECKED_CAST")
class RootCommentViewHolder(
    val binding: ItemCommentCardBinding
) : KSViewHolder(binding.root) {

    private val vm: RootCommentViewHolderViewModel.ViewModel = RootCommentViewHolderViewModel.ViewModel(environment())
    private val ksString = environment().ksString()

    init {
        this.vm.outputs.bindRootComment()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { comment ->
                binding.commentsCardView.setCommentUserName(comment.author().name())
                binding.commentsCardView.setCommentBody(comment.body())
                binding.commentsCardView.hideReplyButton()
                binding.commentsCardView.setCommentPostTime(DateTimeUtils.relative(context(), ksString, comment.createdAt()))
                binding.commentsCardView.setCommentUserName(comment.author().name())
                binding.commentsCardView.setAvatarUrl(comment.author().avatar().medium())
            }
    }

    override fun bindData(data: Any?) {
        if (data is Comment) {
            this.vm.inputs.configureWith(data)
        }
    }
}
