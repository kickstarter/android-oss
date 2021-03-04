package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.databinding.WebViewLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.viewmodels.WebViewViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(WebViewViewModel.ViewModel::class)
class WebViewActivity : BaseActivity<WebViewViewModel.ViewModel>() {
    private lateinit var binding: WebViewLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WebViewLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.outputs.toolbarTitle()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.webViewToolbar.webViewToolbar.setTitle(it) }

        viewModel.outputs.url()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.webView.loadUrl(it) }
    }

    override fun back() {
        // This logic is sound only for web view activities without RequestHandlers.
        // TODO: Refactor the client to update web history properly for activities with RequestHandlers.
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.back()
        }
    }

    override fun exitTransition() = TransitionUtils.slideInFromLeft()
}
