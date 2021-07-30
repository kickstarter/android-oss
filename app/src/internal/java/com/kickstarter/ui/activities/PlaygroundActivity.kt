package com.kickstarter.ui.activities

import android.os.Bundle
import android.view.View
import com.kickstarter.databinding.PlaygroundLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.ui.extensions.snackbar
import com.kickstarter.viewmodels.PlaygroundViewModel

@RequiresActivityViewModel(PlaygroundViewModel.ViewModel::class)
class PlaygroundActivity : BaseActivity<PlaygroundViewModel.ViewModel?>() {
    private lateinit var binding :PlaygroundLayoutBinding
    private lateinit var view :View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlaygroundLayoutBinding.inflate(layoutInflater)
        view = binding.root
        setContentView(view)

        setStepper()
    }

    private fun setStepper() {
        binding.stepper.minimum = 0
        binding.stepper.maximum = 10
        binding.stepper.initialValue = 10
        binding.stepper.stepperListener.onStepperUpdated().subscribe {
            snackbar(view, " $it is the amount on the stepper")
        }
    }
}
