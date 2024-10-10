package com.kickstarter.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Pair
import android.webkit.WebView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.R
import com.kickstarter.databinding.UpdateLayoutBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.Transformers.observeForUIV2
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.isProjectUpdateCommentsUri
import com.kickstarter.libs.utils.extensions.isProjectUpdateUri
import com.kickstarter.libs.utils.extensions.isProjectUri
import com.kickstarter.models.Update
import com.kickstarter.services.RequestHandler
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.UpdateViewModel
import io.reactivex.disposables.CompositeDisposable
import okhttp3.Request

class UpdateActivity : AppCompatActivity() {
    private lateinit var ksString: KSString
    private lateinit var binding: UpdateLayoutBinding

    private lateinit var viewModelFactory: UpdateViewModel.Factory
    private val viewModel: UpdateViewModel.UpdateViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var disposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.getEnvironment()?.let { env ->
            viewModelFactory = UpdateViewModel.Factory(env)
        }
        disposables = CompositeDisposable()

        binding = UpdateLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)

        setUpConnectivityStatusCheck(lifecycle)

        ksString = requireNotNull(getEnvironment()?.ksString())

        binding.updateWebView.registerRequestHandlers(
            listOf(
                RequestHandler({ uri: Uri?, webEndpoint: String ->
                    (uri?.let { it } ?: Uri.EMPTY).isProjectUpdateUri(webEndpoint)
                }) { request: Request, _ -> handleProjectUpdateUriRequest(request) },
                RequestHandler({ uri: Uri?, webEndpoint: String ->
                    (uri?.let { it } ?: Uri.EMPTY).isProjectUpdateCommentsUri(
                        webEndpoint
                    )
                }) { request: Request, _ ->
                    handleProjectUpdateCommentsUriRequest(request)
                },
                RequestHandler({ uri: Uri?, webEndpoint: String ->
                    (uri?.let { it } ?: Uri.EMPTY).isProjectUri(webEndpoint)
                }) { request: Request, webView: WebView -> handleProjectUriRequest(request, webView) }
            )
        )

        // - this.viewModel instantiated in super.onCreate it will never be null at this point
        val viewModel = requireNotNull(this.viewModel)

        viewModel.outputs.openProjectExternally()
            .compose(observeForUIV2())
            .subscribe { projectUrl ->
                openProjectExternally(projectUrl)
            }
            .addToDisposable(disposables)

        viewModel.outputs.hasCommentsDeepLinks()
            .filter { it }
            .compose(observeForUIV2())
            .subscribe {
                viewModel.inputs.goToCommentsActivity()
            }
            .addToDisposable(disposables)

        viewModel.outputs.deepLinkToThreadActivity()
            .filter { it.second == true }
            .map { it.first }
            .compose(observeForUIV2())
            .subscribe {
                viewModel.inputs.goToCommentsActivityToDeepLinkThreadActivity(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.startRootCommentsActivity()
            .compose(observeForUIV2())
            .subscribe { update ->
                startRootCommentsActivity(update)
            }
            .addToDisposable(disposables)

        viewModel.outputs.startRootCommentsActivityToDeepLinkThreadActivity()
            .compose(observeForUIV2())
            .subscribe {
                startRootCommentsActivityToDeepLinkThreadActivity(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.startProjectActivity()
            .compose(observeForUIV2())
            .subscribe { uriAndRefTag ->
                startProjectActivity(uriAndRefTag.first, uriAndRefTag.second)
            }
            .addToDisposable(disposables)

        viewModel.outputs.startShareIntent()
            .compose(observeForUIV2())
            .subscribe { updateAndShareUrl ->
                startShareIntent(updateAndShareUrl)
            }
            .addToDisposable(disposables)

        viewModel.outputs.updateSequence()
            .compose(observeForUIV2())
            .subscribe { updateSequence ->
                binding.updateActivityToolbar.updateToolbar.setTitle(ksString.format(resources.getString(R.string.social_update_number), "update_number", updateSequence))
            }
            .addToDisposable(disposables)

        binding.updateActivityToolbar.shareIconButton.setOnClickListener {
            viewModel.inputs.shareIconButtonClicked()
        }

        viewModel.provideIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.updateWebView.setDelegate(null)
    }

    override fun onResume() {
        super.onResume()

        // - When pressing the url within this webview for seeing updates for a concrete project, this same activity is presented again.
        // we need to reload the webview with the updates url to refresh the UI
        this.viewModel.let { vm ->
            vm.webViewUrl()
                .take(1)
                .subscribe { url ->
                    url?.let {
                        // for thread safety with RX 2
                        binding.updateWebView.post {
                            binding.updateWebView.loadUrl(it)
                        }
                    }
                }
        }
    }

    private fun handleProjectUpdateCommentsUriRequest(request: Request): Boolean {
        this.viewModel.inputs.goToCommentsRequest(request)
        return true
    }

    private fun handleProjectUpdateUriRequest(request: Request): Boolean {
        this.viewModel.inputs.goToUpdateRequest(request)
        return false
    }

    private fun handleProjectUriRequest(request: Request, webView: WebView): Boolean {
        this.viewModel.inputs.goToProjectRequest(request)
        return true
    }

    private fun openProjectExternally(projectUrl: String) {
        ApplicationUtils.openUrlExternally(this, projectUrl)
    }

    private fun startRootCommentsActivity(update: Update) {
        val intent = Intent(this, CommentsActivity::class.java)
            .putExtra(IntentKey.UPDATE, update)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startRootCommentsActivityToDeepLinkThreadActivity(data: Pair<String, Update>) {
        val intent = Intent(this, CommentsActivity::class.java)
            .putExtra(IntentKey.COMMENT, data.first)
            .putExtra(IntentKey.UPDATE, data.second)

        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startProjectActivity(uri: Uri, refTag: RefTag) {
        val intent = Intent().getProjectIntent(this)
            .setData(uri)
            .putExtra(IntentKey.REF_TAG, refTag)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startShareIntent(updateAndShareUrl: Pair<Update, String>) {
        val update = updateAndShareUrl.first
        val shareUrl = updateAndShareUrl.second
        val shareMessage = (
            ksString.format(resources.getString(R.string.activity_project_update_update_count), "update_count", NumberUtils.format(update.sequence())) +
                ": " + update.title()
            )
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "$shareMessage $shareUrl")
        startActivity(Intent.createChooser(intent, getString(R.string.Share_update)))
    }
}
