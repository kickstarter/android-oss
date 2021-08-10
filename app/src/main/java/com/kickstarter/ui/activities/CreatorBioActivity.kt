package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.annotation.NonNull
import androidx.core.view.isGone
import com.kickstarter.databinding.ActivityCreatorBioBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.CreatorBioViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(CreatorBioViewModel.ViewModel::class)
class CreatorBioActivity : BaseActivity<CreatorBioViewModel.ViewModel>() {

    private lateinit var binding: ActivityCreatorBioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatorBioBinding.inflate(layoutInflater)

        setContentView(binding.root)

        this.viewModel.outputs.messageIconIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe {
                binding.messageButton.isGone = it
            }

        this.viewModel.outputs.url()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { binding.webView.loadUrl(it) }

        this.viewModel.outputs.startComposeMessageActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { startComposeMessageActivity(it) }

        this.viewModel.outputs.startMessagesActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { startMessagesActivity(it) }

        binding.messageButton.setOnClickListener {
            this.viewModel.inputs.messageButtonClicked()
        }
    }

    @NonNull
    override fun exitTransition(): Pair<Int, Int>? {
        return slideInFromLeft()
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
