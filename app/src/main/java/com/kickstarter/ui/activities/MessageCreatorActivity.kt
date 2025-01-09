package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
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
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMessageCreatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
                topMargin = insets.top
            }

            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(bottom = imeInsets.bottom)

            WindowInsetsCompat.CONSUMED
        }

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
