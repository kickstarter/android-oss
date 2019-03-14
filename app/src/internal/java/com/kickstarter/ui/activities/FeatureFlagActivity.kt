package com.kickstarter.ui.activities

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.R
import com.kickstarter.libs.preferences.BooleanPreferenceType
import com.kickstarter.libs.qualifiers.HorizontalRewards
import kotlinx.android.synthetic.internal.activity_feature_flag.*
import javax.inject.Inject

class FeatureFlagActivity : AppCompatActivity() {

    @Inject
    @HorizontalRewards
    protected lateinit var enableHorizontalRewards: BooleanPreferenceType

    @Inject lateinit var environment: Environment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_flag)

        val enabled = enableHorizontalRewards.get()
        if (enabled) {
            horizontal_switch.isChecked = enabled
        }

        horizontal_switch.setOnClickListener {
            enableHorizontalScrolling()
        }
    }

    private fun enableHorizontalScrolling() {
        val isEnabled = horizontal_switch.isChecked
        enableHorizontalRewards.set(isEnabled)
    }
}
