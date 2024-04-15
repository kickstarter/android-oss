package com.kickstarter.ui.viewholders

import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isGone
import com.kickstarter.databinding.MessageViewBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Message
import com.kickstarter.ui.activities.DeepLinkActivity
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.viewmodels.MessageHolderViewModel
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
            .subscribe { setText(it) }
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

    override fun destroy() {
        disposables.clear()
        super.destroy()
    }

    private fun setText(text: String) {
        binding.messageBodyRecipientTextView.text = text
        val tvText: CharSequence = binding.messageBodyRecipientTextView.text
        if (tvText is Spannable) {
            val end = tvText.length
            val urls = tvText.getSpans(0, end, URLSpan::class.java)
            val style = SpannableStringBuilder(tvText)
            style.clearSpans()
            for (urlSpan in urls) {
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(view: View) {
                        val intent = Intent(view.context, DeepLinkActivity::class.java)
                        intent.data = Uri.parse(urlSpan.url)
                        startActivity(view.context, intent, null)
                    }
                }
                style.setSpan(
                    clickableSpan, tvText.getSpanStart(urlSpan),
                    tvText.getSpanEnd(urlSpan),
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }
            binding.messageBodyRecipientTextView.text = style
        }
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
        binding.messageSenderAvatarImageView.loadCircleImage(avatarUrl)
    }
}
