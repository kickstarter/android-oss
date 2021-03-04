package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.MessageViewBinding
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Message
import com.kickstarter.viewmodels.MessageHolderViewModel
import com.squareup.picasso.Picasso

class MessageViewHolder(private val binding: MessageViewBinding) : KSViewHolder(binding.root) {
    private val viewModel = MessageHolderViewModel.ViewModel(environment())

    init {
        viewModel.outputs.deliveryStatusTextViewIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.messageDeliveryStatusTextView))

        viewModel.outputs.messageBodyRecipientCardViewIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.messageBodyRecipientCardView))

        viewModel.outputs.messageBodyRecipientTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.messageBodyRecipientTextView.text = it }

        viewModel.outputs.messageBodySenderCardViewIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.messageBodySenderCardView))

        viewModel.outputs.messageBodySenderTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.messageBodySenderTextView.text = it }

        viewModel.outputs.participantAvatarImageHidden()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.messageSenderAvatarImageView))

        viewModel.outputs.participantAvatarImageUrl()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setParticipantAvatarImageView(it) }
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val message = ObjectUtils.requireNonNull(data as Message?)
        viewModel.inputs.configureWith(message)
    }

    fun isLastPosition(isLastPosition: Boolean) {
        viewModel.inputs.isLastPosition(isLastPosition)
    }

    private fun setParticipantAvatarImageView(avatarUrl: String) {
        Picasso.get().load(avatarUrl)
            .transform(CircleTransformation())
            .into(binding.messageSenderAvatarImageView)
    }
}
