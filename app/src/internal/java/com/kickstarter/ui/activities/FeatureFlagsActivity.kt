package com.kickstarter.ui.activities

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.preferences.BooleanPreferenceType
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.ui.adapters.FeatureFlagAdapter
import com.kickstarter.ui.itemdecorations.TableItemDecoration
import com.kickstarter.viewmodels.FeatureFlagsViewModel
import kotlinx.android.synthetic.internal.activity_feature_flags.*
import kotlinx.android.synthetic.main.item_feature_flag_override.view.*

@RequiresActivityViewModel(FeatureFlagsViewModel.ViewModel::class)
class FeatureFlagsActivity : BaseActivity<FeatureFlagsViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_flags)

        val featureFlagAdapter = FeatureFlagAdapter()
        config_flags.adapter = featureFlagAdapter
        config_flags.addItemDecoration(TableItemDecoration())

        this.viewModel.outputs.features()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { featureFlagAdapter.takeFlags(it) }

        displayPreference(R.string.Native_Checkout, environment().nativeCheckoutPreference(), native_checkout)
    }

    @Suppress("SameParameterValue")
    private fun displayPreference(@StringRes labelRes: Int, booleanPreferenceType: BooleanPreferenceType, overrideContainer: View) {
        val enabled = booleanPreferenceType.get()
        overrideContainer.override_label.setText(labelRes)
        val switch = overrideContainer.override_switch
        if (enabled) {
            switch.isChecked = enabled
        }

        overrideContainer.setOnClickListener {
            val isEnabled = switch.isChecked
            booleanPreferenceType.set(isEnabled)
        }
    }
}
