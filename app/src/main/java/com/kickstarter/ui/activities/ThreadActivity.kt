package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.databinding.ActivityThreadLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.viewmodels.ThreadViewModel

@RequiresActivityViewModel(ThreadViewModel.ViewModel::class)
class ThreadActivity : BaseActivity<ThreadViewModel.ViewModel>() {
    private lateinit var binding: ActivityThreadLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityThreadLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
