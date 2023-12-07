package com.kickstarter.ui.viewholders

import androidx.core.view.isGone
import com.kickstarter.databinding.MessageViewBinding
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Message
import com.kickstarter.viewmodels.MessageHolderViewModel
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class MessageViewHolder(private val binding: MessageViewBinding) : KSViewHolder(binding.root) {
    private val viewModel = MessageHolderViewModel.ViewModel(environment())
    private val disposables = CompositeDisposable()

    init {
        viewModel.outputs.deliveryStatusTextViewIsGone()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { binding.messageDeliveryStatusTextView.isGone = it }
                .addToDisposable(disposables)

        viewModel.outputs.messageBodyRecipientCardViewIsGone()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { binding.messageBodyRecipientCardView.isGone = it }
                .addToDisposable(disposables)

        viewModel.outputs.messageBodyRecipientTextViewText()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { binding.messageBodyRecipientTextView.text = it }
                .addToDisposable(disposables)

        viewModel.outputs.messageBodySenderCardViewIsGone()
                .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messageBodySenderCardView.isGone = it }
                .addToDisposable(disposables)

        viewModel.outputs.messageBodySenderTextViewText()
                .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messageBodySenderTextView.text = it }
                .addToDisposable(disposables)

        viewModel.outputs.participantAvatarImageHidden()
                .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.messageSenderAvatarImageView.isGone = it }
                .addToDisposable(disposables)

        viewModel.outputs.participantAvatarImageUrl()
                .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setParticipantAvatarImageView(it) }
                .addToDisposable(disposables)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val message = requireNotNull(data as Message?)
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
