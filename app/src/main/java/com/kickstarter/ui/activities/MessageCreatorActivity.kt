package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityMessageCreatorBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.MessageThread
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.MessageCreatorViewModel

@RequiresActivityViewModel(MessageCreatorViewModel.ViewModel::class)
class MessageCreatorActivity : BaseActivity<MessageCreatorViewModel.ViewModel>() {
    private lateinit var ksString: KSString
    private lateinit var binding: ActivityMessageCreatorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageCreatorBinding.inflate(layoutInflater)

        setContentView(binding.root)

        this.ksString = requireNotNull(this.environment().ksString())

        this.viewModel.outputs.showSentError()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { showSnackbar(binding.messageBody, it) }

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
            .subscribe {
                binding.progressBar.isGone = !it
            }

        this.viewModel.outputs.sendButtonIsEnabled()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { binding.sendMessageButton.isEnabled = it }

        this.viewModel.outputs.showMessageThread()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { finishAndStartMessagesActivity(it) }

        binding.messageBody.onChange { this.viewModel.inputs.messageBodyChanged(it) }

        binding.sendMessageButton.setOnClickListener {
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
                .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, MessagePreviousScreenType.CREATOR_BIO_MODAL)
        )
    }

    private fun setHint(hint: String) {
        binding.messageBodyTil.hint = this.ksString.format(getString(R.string.Message_user_name), "user_name", hint)
    }
}
