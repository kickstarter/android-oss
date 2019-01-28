package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.viewmodels.CreatePasswordViewModel
import kotlinx.android.synthetic.main.create_password_toolbar.*

@RequiresActivityViewModel(CreatePasswordViewModel.ViewModel::class)
class CreatePasswordActivity : BaseActivity<CreatePasswordViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_password)
        setSupportActionBar(create_password_toolbar)
    }
}
