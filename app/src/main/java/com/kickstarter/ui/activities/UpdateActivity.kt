package com.kickstarter.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Pair
import android.webkit.WebView
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.models.Update
import com.kickstarter.services.KSUri
import com.kickstarter.services.RequestHandler
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.views.KSWebView
import com.kickstarter.viewmodels.UpdateViewModel
import kotlinx.android.synthetic.main.update_layout.update_web_view
import kotlinx.android.synthetic.main.update_toolbar.share_icon_button
import kotlinx.android.synthetic.main.update_toolbar.update_toolbar
import okhttp3.Request


@RequiresActivityViewModel(UpdateViewModel.ViewModel::class)
class UpdateActivity : BaseActivity<UpdateViewModel.ViewModel?>(), KSWebView.Delegate {
    private lateinit var ksString: KSString

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_layout)
        ksString = environment().ksString()

        update_web_view.setDelegate(this)
        update_web_view.registerRequestHandlers(
                listOf(
                        RequestHandler({ uri: Uri?, webEndpoint: String ->
                            KSUri.isProjectUpdateUri(uri?.let { it } ?: Uri.EMPTY, webEndpoint)
                        })
                        { request: Request, _ -> handleProjectUpdateUriRequest(request) },
                        RequestHandler({ uri: Uri?, webEndpoint: String ->
                            KSUri.isProjectUpdateCommentsUri(uri?.let { it }
                                    ?: Uri.EMPTY, webEndpoint)
                        })
                        { request: Request, _ -> handleProjectUpdateCommentsUriRequest(request) },
                        RequestHandler({ uri: Uri?, webEndpoint: String ->
                            KSUri.isProjectUri(uri?.let { it } ?: Uri.EMPTY, webEndpoint)
                        })
                        { request: Request, webView: WebView -> handleProjectUriRequest(request, webView) }
                )
        )

        // - this.viewModel instantiated in super.onCreate it will never be null at this point
        val viewModel = requireNotNull(this.viewModel)

        viewModel.outputs.openProjectExternally()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { projectUrl ->
                    openProjectExternally(projectUrl)
                }

        viewModel.outputs.startCommentsActivity()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { update ->
                    startCommentsActivity(update)
                }

        viewModel.outputs.startProjectActivity()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { uriAndRefTag ->
                    startProjectActivity(uriAndRefTag.first, uriAndRefTag.second)
                }

        viewModel.outputs.startShareIntent()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { updateAndShareUrl ->
                    startShareIntent(updateAndShareUrl)
                }

        viewModel.outputs.updateSequence()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { updateSequence ->
                    update_toolbar.setTitle(ksString.format(resources.getString(R.string.social_update_number), "update_number", updateSequence))
                }

        share_icon_button.setOnClickListener {
            viewModel.inputs.shareIconButtonClicked()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        update_web_view.setDelegate(null)
        this.viewModel = null
    }

    override fun onResume() {
        super.onResume()

        // - When pressing the url within this webview for seeing updates for a concrete project, this same activity is presented again.
        // we need to reload the webview with the updates url to refresh the UI
        this.viewModel?.let { vm ->
            vm.webViewUrl()
                    .take(1)
                    .compose(bindToLifecycle())
                    .compose(observeForUI())
                    .subscribe { url ->
                        url?.let {
                            update_web_view.loadUrl(it)
                        }
                    }

        }
    }


    private fun handleProjectUpdateCommentsUriRequest(request: Request): Boolean {
        this.viewModel?.inputs?.goToCommentsRequest(request)
        return true
    }

    private fun handleProjectUpdateUriRequest(request: Request): Boolean {
        this.viewModel?.inputs?.goToUpdateRequest(request)
        return false
    }

    private fun handleProjectUriRequest(request: Request, webView: WebView): Boolean {
        this.viewModel?.inputs?.goToProjectRequest(request)
        return true
    }

    private fun openProjectExternally(projectUrl: String) {
        ApplicationUtils.openUrlExternally(this, projectUrl)
    }

    private fun startCommentsActivity(update: Update) {
        val intent = Intent(this, CommentsActivity::class.java)
                .putExtra(IntentKey.UPDATE, update)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startProjectActivity(uri: Uri, refTag: RefTag) {
        val intent = Intent(this, ProjectActivity::class.java)
                .setData(uri)
                .putExtra(IntentKey.REF_TAG, refTag)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startShareIntent(updateAndShareUrl: Pair<Update, String>) {
        val update = updateAndShareUrl.first
        val shareUrl = updateAndShareUrl.second
        val shareMessage = (ksString.format(resources.getString(R.string.activity_project_update_update_count), "update_count", NumberUtils.format(update.sequence()))
                + ": " + update.title())
        val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "$shareMessage $shareUrl")
        startActivity(Intent.createChooser(intent, getString(R.string.Share_update)))
    }

    override fun exitTransition(): Pair<Int, Int>? {
        return TransitionUtils.slideInFromLeft()
    }

    override fun externalLinkActivated(url: String) {
        this.viewModel?.inputs?.externalLinkActivated()
    }

    override fun pageIntercepted(url: String) {}
    override fun onReceivedError(url: String) {}
}