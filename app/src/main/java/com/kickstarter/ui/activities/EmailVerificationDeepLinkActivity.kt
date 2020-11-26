package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.viewmodels.EmailVerificationDeepLinkViewModel

@RequiresActivityViewModel(EmailVerificationDeepLinkViewModel.ViewModel::class)
class EmailVerificationDeepLinkActivity : BaseActivity<EmailVerificationDeepLinkViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.viewModel.outputs.openDiscoveryActivityWith()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    startDiscoveryActivity(it)
                }
    }

    private fun startDiscoveryActivity(pair: Pair<Int, String>) {
        ApplicationUtils.startNewDiscoveryActivity(this)
        finish()
    }
}