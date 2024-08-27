package com.kickstarter.ui.viewholders

import android.content.Intent
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import com.kickstarter.R
import com.kickstarter.databinding.MessageThreadViewBinding
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.wrapInParentheses
import com.kickstarter.models.MessageThread
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.MessagesActivity
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.viewmodels.MessageThreadHolderViewModel
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTime

class MessageThreadViewHolder(private val binding: MessageThreadViewBinding) : KSViewHolder(binding.root) {
    private val viewModel = MessageThreadHolderViewModel.ViewModel(environment())
    private val ksString = requireNotNull(environment().ksString())
    private val disposables = CompositeDisposable()

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val messageThread = requireNotNull(data as MessageThread?)
        viewModel.inputs.configureWith(messageThread)
    }

    private fun setDateTextView(date: DateTime) {
        binding.messageThreadDateTextView.text = DateTimeUtils.relative(context(), ksString, date)
    }

    private fun setTypeface(textView: TextView, bold: Boolean) {
        val style = if (bold) Typeface.BOLD else Typeface.NORMAL
        textView.typeface = Typeface.create(textView.typeface, style)
    }

    private fun startMessagesActivity(messageThread: MessageThread) {
        val intent = Intent(context(), MessagesActivity::class.java)
            .putExtra(IntentKey.MESSAGE_THREAD, messageThread)
            .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, MessagePreviousScreenType.MESSAGES)
        context().startActivity(intent)
    }

    private fun setParticipantAvatarImageView(avatarUrl: String) {
        binding.participantAvatarImageView.loadCircleImage(avatarUrl)
    }

    private fun setUnreadCountTextView(unreadCount: String) {
        binding.messageThreadUnreadCountTextView.text = unreadCount.wrapInParentheses()
        binding.messageThreadUnreadCountTextView.contentDescription = ksString.format(context().getString(R.string.unread_count_unread), "unread_count", unreadCount)
    }

    init {
        binding.messageThreadContainer.setOnClickListener {
            viewModel.inputs.messageThreadCardViewClicked()
        }
        viewModel.outputs.dateDateTime()
            .compose(Transformers.observeForUIV2())
            .subscribe { setDateTextView(it) }
            .addToDisposable(disposables)
        viewModel.outputs.dateTextViewIsBold()
            .compose(Transformers.observeForUIV2())
            .subscribe { setTypeface(binding.messageThreadDateTextView, it) }
            .addToDisposable(disposables)
        viewModel.outputs.messageBodyTextViewText()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.messageThreadBodyTextView.text = it }
            .addToDisposable(disposables)
        viewModel.outputs.messageBodyTextIsBold()
            .compose(Transformers.observeForUIV2())
            .subscribe { setTypeface(binding.messageThreadBodyTextView, it) }
            .addToDisposable(disposables)
        viewModel.outputs.participantAvatarUrl()
            .compose(Transformers.observeForUIV2())
            .subscribe { setParticipantAvatarImageView(it) }
            .addToDisposable(disposables)
        viewModel.outputs.participantNameTextViewIsBold()
            .compose(Transformers.observeForUIV2())
            .subscribe { setTypeface(binding.participantNameTextView, it) }
            .addToDisposable(disposables)
        viewModel.outputs.participantNameTextViewText()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.participantNameTextView.text = it }
            .addToDisposable(disposables)
        viewModel.outputs.startMessagesActivity()
            .compose(Transformers.observeForUIV2())
            .subscribe { startMessagesActivity(it) }
            .addToDisposable(disposables)
        viewModel.outputs.unreadCountTextViewIsGone()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.messageThreadUnreadCountTextView.visibility = View.GONE }
            .addToDisposable(disposables)
        viewModel.outputs.unreadCountTextViewText()
            .compose(Transformers.observeForUIV2())
            .subscribe { setUnreadCountTextView(it) }
            .addToDisposable(disposables)
    }

    override fun destroy() {
        disposables.clear()
        super.destroy()
    }
}
