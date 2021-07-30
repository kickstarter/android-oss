package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.databinding.PlaygroundLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.viewmodels.PlaygroundViewModel

@RequiresActivityViewModel(PlaygroundViewModel.ViewModel::class)
class PlaygroundActivity : BaseActivity<PlaygroundViewModel.ViewModel?>() {
    val binding = PlaygroundLayoutBinding.inflate(layoutInflater)
    val view = binding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view)

        setStepper()
    }

    private fun setStepper() {
        binding.stepper.minimum = 0
        binding.stepper.maximum = 10
        binding.stepper.initialValue = 10
    }
}
