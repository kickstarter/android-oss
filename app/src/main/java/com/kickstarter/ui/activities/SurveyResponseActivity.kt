package com.kickstarter.ui.activities

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.kickstarter.R
import com.kickstarter.databinding.SurveyResponseLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.services.KSUri
import com.kickstarter.services.RequestHandler
import com.kickstarter.viewmodels.SurveyResponseViewModel
import okhttp3.Request
import java.util.*

@RequiresActivityViewModel(SurveyResponseViewModel.ViewModel::class)
class SurveyResponseActivity : BaseActivity<SurveyResponseViewModel.ViewModel>() {

    private lateinit var binding: SurveyResponseLayoutBinding

    private val confirmationDialog: AlertDialog by lazy {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.Got_it_your_survey_response_has_been_submitted))
            .setPositiveButton(
                getString(R.string.general_alert_buttons_ok)
            ) { _: DialogInterface?, _: Int -> viewModel.inputs.okButtonClicked() }
            .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SurveyResponseLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.surveyResponseWebView.registerRequestHandlers(
            Arrays.asList(
                RequestHandler(
                    { uri: Uri, webEndpoint: String ->
                        KSUri.isProjectSurveyUri(uri, webEndpoint)
                    }) { request: Request, webView: WebView ->
                    handleProjectSurveyUriRequest(request, webView)
                },
                RequestHandler({ uri: Uri, webEndpoint: String ->
                    KSUri.isProjectUri(uri, webEndpoint)
                }) { request: Request, webView: WebView ->
                    handleProjectUriRequest(request, webView)
                }
            )
        )

        viewModel.outputs.goBack()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { back() }

        viewModel.outputs.showConfirmationDialog()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { confirmationDialog.show() }
    }

    private fun handleProjectUriRequest(request: Request, webView: WebView): Boolean {
        viewModel.inputs.projectUriRequest(request)
        return true
    }

    private fun handleProjectSurveyUriRequest(request: Request, webView: WebView): Boolean {
        viewModel.inputs.projectSurveyUriRequest(request)
        return false
    }

    override fun onResume() {
        super.onResume()
        viewModel.outputs.webViewUrl()
            .take(1)
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.surveyResponseWebView.loadUrl(it) }
    }
}
