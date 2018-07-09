package com.kickstarter.ui.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.databinding.SettingsLayoutNewBinding
import com.kickstarter.viewmodels.SettingsNewViewModel

class SettingsNewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: SettingsLayoutNewBinding = DataBindingUtil.setContentView(this, R.layout.settings_layout_new)

        val environment = (application as KSApplication).component().environment()
        binding.viewModel = SettingsNewViewModel(this, environment)
    }
}