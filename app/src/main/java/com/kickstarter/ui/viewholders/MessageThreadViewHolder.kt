package com.kickstarter.ui.viewholders

import android.content.Intent
import android.graphics.Typeface
import android.widget.TextView
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.MessageThreadViewBinding
import com.kickstarter.libs.KoalaContext
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.wrapInParentheses
import com.kickstarter.models.MessageThread
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.MessagesActivity
import com.kickstarter.viewmodels.MessageThreadHolderViewModel
import com.squareup.picasso.Picasso
import org.joda.time.DateTime

class MessageThreadViewHolder(private val binding: MessageThreadViewBinding) : KSViewHolder(binding.root) {
    private val viewModel = MessageThreadHolderViewModel.ViewModel(environment())
    private val ksString = environment().ksString()

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val messageThread = ObjectUtils.requireNonNull(data as MessageThread?)
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
            .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Message.MESSAGES)
        context().startActivity(intent)
    }

    private fun setParticipantAvatarImageView(avatarUrl: String) {
        Picasso.with(context()).load(avatarUrl)
            .transform(CircleTransformation())
            .into(binding.participantAvatarImageView)
    }

    private fun setUnreadCountTextView(unreadCount: String) {
        binding.messageThreadUnreadCountTextView.text = unreadCount.wrapInParentheses()
        binding.messageThreadUnreadCountTextView.contentDescription = ksString.format(context().getString(R.string.unread_count_unread), "unread_count", unreadCount)
    }

    init {
        RxView.clicks(binding.messageThreadContainer)
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { viewModel.inputs.messageThreadCardViewClicked() }
        viewModel.outputs.dateDateTime()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setDateTextView(it) }
        viewModel.outputs.dateTextViewIsBold()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setTypeface(binding.messageThreadDateTextView, it) }
        viewModel.outputs.messageBodyTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.messageThreadBodyTextView.text = it }
        viewModel.outputs.messageBodyTextIsBold()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setTypeface(binding.messageThreadBodyTextView, it) }
        viewModel.outputs.participantAvatarUrl()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setParticipantAvatarImageView(it) }
        viewModel.outputs.participantNameTextViewIsBold()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setTypeface(binding.participantNameTextView, it) }
        viewModel.outputs.participantNameTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.participantNameTextView.text = it }
        viewModel.outputs.startMessagesActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startMessagesActivity(it) }
        viewModel.outputs.unreadCountTextViewIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setGone(binding.messageThreadUnreadCountTextView))
        viewModel.outputs.unreadCountTextViewText()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setUnreadCountTextView(it) }
    }
}
