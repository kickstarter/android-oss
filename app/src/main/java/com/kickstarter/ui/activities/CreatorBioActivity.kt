package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KoalaContext
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.CreatorBioViewModel
import kotlinx.android.synthetic.main.activity_creator_bio.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(CreatorBioViewModel.ViewModel::class)
class CreatorBioActivity : BaseActivity<CreatorBioViewModel.ViewModel>() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator_bio)

        this.viewModel.outputs.messageIconIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { ViewUtils.setGone(message_button, it) }

        this.viewModel.outputs.url()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { web_view.loadUrl(it) }

        this.viewModel.outputs.startComposeMessageActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { startComposeMessageActivity(it) }

        this.viewModel.outputs.startMessagesActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe { startMessagesActivity(it) }

        message_button.setOnClickListener {
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
                .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Message.CREATOR_BIO_MODAL)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.BACKING, project.backing())
        )
    }
}
