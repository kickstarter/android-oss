package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.viewmodels.EmailVerificationDeepLinkViewModel

@RequiresActivityViewModel(EmailVerificationDeepLinkViewModel.ViewModel::class)
class EmailVerificationDeepLinkActivity : BaseActivity<EmailVerificationDeepLinkViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}