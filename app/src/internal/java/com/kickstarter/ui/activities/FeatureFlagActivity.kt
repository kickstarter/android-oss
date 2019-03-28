package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.libs.Environment
import kotlinx.android.synthetic.internal.activity_feature_flag.*
import javax.inject.Inject

class FeatureFlagActivity : AppCompatActivity() {
    @Inject lateinit var environment: Environment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_flag)

        (applicationContext as KSApplication).component().inject(this)

        val enabled = environment.horizontalRewardsEnabled().get()
        if (enabled) {
            horizontal_switch.isChecked = enabled
        }

        horizontal_switch.setOnClickListener { enableHorizontalRewards() }
    }

    private fun enableHorizontalRewards() {
        val isEnabled = horizontal_switch.isChecked
        this.environment.horizontalRewardsEnabled().set(isEnabled)
    }
}
