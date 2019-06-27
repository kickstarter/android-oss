package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.preferences.BooleanPreferenceType
import kotlinx.android.synthetic.internal.activity_feature_flag.*
import javax.inject.Inject

class FeatureFlagActivity : AppCompatActivity() {
    @Inject lateinit var environment: Environment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_flag)

        (applicationContext as KSApplication).component().inject(this)
        displayPreference(environment.nativeCheckoutPreference(), native_checkout_switch)
    }

    private fun displayPreference(booleanPreferenceType: BooleanPreferenceType, switchCompat: SwitchCompat) {
        val enabled = booleanPreferenceType.get()
        if (enabled) {
            switchCompat.isChecked = enabled
        }

        switchCompat.setOnClickListener {
            val isEnabled = switchCompat.isChecked
            booleanPreferenceType.set(isEnabled)
        }
    }
}
