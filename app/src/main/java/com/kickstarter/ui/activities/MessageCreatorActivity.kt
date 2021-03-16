package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.KoalaContext
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.MessageThread
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.MessageCreatorViewModel
import kotlinx.android.synthetic.main.activity_message_creator.*

@RequiresActivityViewModel(MessageCreatorViewModel.ViewModel::class)
class MessageCreatorActivity : BaseActivity<MessageCreatorViewModel.ViewModel>() {
    private lateinit var ksString: KSString

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_creator)

        this.ksString = this.environment().ksString()

        this.viewModel.outputs.showSentError()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { showSnackbar(message_body, it) }

        this.viewModel.outputs.showSentSuccess()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { finishAndShowSuccessToast(it) }

        this.viewModel.outputs.creatorName()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { setHint(it) }

        this.viewModel.outputs.progressBarIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { ViewUtils.setGone(progress_bar, !it) }

        this.viewModel.outputs.sendButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { send_message_button.isEnabled = it }

        this.viewModel.outputs.showMessageThread()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { finishAndStartMessagesActivity(it) }

        message_body.onChange { this.viewModel.inputs.messageBodyChanged(it) }

        send_message_button.setOnClickListener {
            this.viewModel.inputs.sendButtonClicked()
        }
    }

    private fun finishAndShowSuccessToast(successStringRes: Int) {
        finish()
        ViewUtils.showToast(this, getString(successStringRes))
    }

    private fun finishAndStartMessagesActivity(messageThread: MessageThread) {
        finish()
        startActivity(
            Intent(this, MessagesActivity::class.java)
                .putExtra(IntentKey.MESSAGE_THREAD, messageThread)
                .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Message.CREATOR_BIO_MODAL)
        )
    }

    private fun setHint(hint: String) {
        message_body_til.hint = this.ksString.format(getString(R.string.Message_user_name), "user_name", hint)
    }
}
