package com.kickstarter.ui.activities

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.databinding.ActivityHelpNewBinding
import com.kickstarter.viewmodels.HelpNewViewModel

class HelpNewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityHelpNewBinding = DataBindingUtil.setContentView(this, R.layout.activity_help_new)
        val environment = (application as KSApplication).component().environment()
        binding.viewModel = HelpNewViewModel(this, environment)
    }
}
