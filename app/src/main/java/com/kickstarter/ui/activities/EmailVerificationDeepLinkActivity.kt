package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
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
        val intent = Intent(this, DiscoveryActivity::class.java)
                .setAction(Intent.ACTION_MAIN)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("VERIFICATION_CODE", pair.first)
                .putExtra("VERIFICATION_MESSAGE", pair.second)

        startActivity(intent)
        finish()
    }
}