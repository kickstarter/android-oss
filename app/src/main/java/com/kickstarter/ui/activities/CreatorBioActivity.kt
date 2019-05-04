package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.AnimationUtils
import com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft
import com.kickstarter.services.KSWebViewClient
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.CreatorBioViewModel
import kotlinx.android.synthetic.main.activity_creator_bio.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(CreatorBioViewModel.ViewModel::class)
class CreatorBioActivity : BaseActivity<CreatorBioViewModel.ViewModel>(), KSWebViewClient.Delegate {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator_bio)

        web_view.client().setDelegate(this)

        this.viewModel.outputs.url()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{ web_view.loadUrl(it) }

        this.viewModel.outputs.startComposeMessageActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    startActivity(Intent(this, ComposeMessageActivity::class.java)
                        .putExtra(IntentKey.PROJECT, it))
                }

        this.viewModel.outputs.startMessageActivity()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    startActivity(Intent(this, MessagesActivity::class.java)
                            .putExtra(IntentKey.PROJECT, it)
                            .putExtra(IntentKey.BACKING, it.backing()))
                }

        message_button.setOnClickListener {
            this.viewModel.inputs.messageButtonClicked()
        }
    }

    override fun webViewExternalLinkActivated(@NonNull webViewClient: KSWebViewClient, @NonNull url: String) {}

    override fun webViewOnPageStarted(@NonNull webViewClient: KSWebViewClient, @Nullable url: String?) {
        loading_indicator_view.startAnimation(AnimationUtils.appearAnimation())
    }

    override fun webViewOnPageFinished(@NonNull webViewClient: KSWebViewClient, @Nullable url: String?) {
        loading_indicator_view.startAnimation(AnimationUtils.disappearAnimation())
    }

    override fun webViewPageIntercepted(@NonNull webViewClient: KSWebViewClient, @Nullable url: String) {}

    @NonNull
    override fun exitTransition(): Pair<Int, Int>? {
        return slideInFromLeft()
    }

}
