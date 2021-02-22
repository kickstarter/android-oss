package com.kickstarter.ui.activities

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.kickstarter.databinding.DownloadBetaLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.viewmodels.DownloadBetaViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(DownloadBetaViewModel::class)
class DownloadBetaActivity : BaseActivity<DownloadBetaViewModel>() {
    private lateinit var binding: DownloadBetaLayoutBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DownloadBetaLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val build = viewModel.outputs.internalBuildEnvelope()
            .map { it.build() }
            .filter { ObjectUtils.isNotNull(it) }
            .map { it.toString() }

        build
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.build.text = it }

        build
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { requestDownload(it) }

        viewModel.outputs.internalBuildEnvelope()
            .map { it.changelog() }
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.changelog.text = it }

        binding.openDownloadsButton.setOnClickListener {
            openDownloadsOnClick()
        }
    }

    private fun openDownloadsOnClick() {
        val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
        startActivity(intent)
    }

    private fun requestDownload(build: String) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse("https://www.kickstarter.com/mobile/beta/builds/$build"))
        startActivity(intent)
    }
}
