package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.kickstarter.R
import com.kickstarter.databinding.ActivityMessageCreatorBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.MessageThread
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.MessageCreatorViewModel.Factory
import com.kickstarter.viewmodels.MessageCreatorViewModel.MessageCreatorViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class MessageCreatorActivity : AppCompatActivity() {
    private lateinit var ksString: KSString
    private lateinit var binding: ActivityMessageCreatorBinding

    private lateinit var viewModelFactory: Factory
    private val viewModel: MessageCreatorViewModel by viewModels { viewModelFactory }

    private var disposables = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageCreatorBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)

        setUpConnectivityStatusCheck(lifecycle)

        val environment = this.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env, intent = intent)
            env
        }

        this.ksString = requireNotNull(environment?.ksString())

        this.viewModel.outputs.showSentError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSnackbar(binding.messageBody, it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.showSentSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { finishAndShowSuccessToast(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.creatorName()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setHint(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.progressBarIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.progressBar.isGone = !it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.sendButtonIsEnabled()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.sendMessageButton.isEnabled = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.showMessageThread()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { finishAndStartMessagesActivity(it) }
            .addToDisposable(disposables)

        binding.messageBody.onChange { this.viewModel.inputs.messageBodyChanged(it) }

        binding.sendMessageButton.setOnClickListener {
            this.viewModel.inputs.sendButtonClicked()
        }

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
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
