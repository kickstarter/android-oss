package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.viewmodels.ChangeEmailViewModel

@RequiresActivityViewModel(ChangeEmailViewModel.ViewModel::class)
class ChangeEmailActivity : BaseActivity<ChangeEmailViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)
    }
}
