package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.kickstarter.databinding.ActivityCreatorBioBinding
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.CreatorBioViewModel.CreatorBioViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class CreatorBioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatorBioBinding
    private lateinit var viewModelFactory: CreatorBioViewModel.Factory
    private val viewModel: CreatorBioViewModel by viewModels { viewModelFactory }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatorBioBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        this.getEnvironment()?.let { env ->
            viewModelFactory = CreatorBioViewModel.Factory(env, intent = intent)
            env
        }

        setContentView(binding.root)

        this.viewModel.outputs.messageIconIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.messageButton.isGone = it
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.url()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.webView.loadUrl(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.startComposeMessageActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startComposeMessageActivity(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.startMessagesActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startMessagesActivity(it) }
            .addToDisposable(disposables)

        binding.messageButton.setOnClickListener {
            this.viewModel.inputs.messageButtonClicked()
        }

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
    private fun startComposeMessageActivity(it: Project?) {
        startActivity(
            Intent(this, MessageCreatorActivity::class.java)
                .putExtra(IntentKey.PROJECT, it)
        )
    }

    private fun startMessagesActivity(project: Project) {
        startActivity(
            Intent(this, MessagesActivity::class.java)
                .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, MessagePreviousScreenType.CREATOR_BIO_MODAL)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.BACKING, project.backing())
        )
    }
}
