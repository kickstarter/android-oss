package com.kickstarter.ui.activities

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.preferences.BooleanPreferenceType
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.ui.adapters.FeatureFlagsAdapter
import com.kickstarter.ui.itemdecorations.TableItemDecoration
import com.kickstarter.viewmodels.FeatureFlagsViewModel
import kotlinx.android.synthetic.internal.activity_feature_flags.*
import kotlinx.android.synthetic.internal.item_feature_flag_override.view.*

@RequiresActivityViewModel(FeatureFlagsViewModel.ViewModel::class)
class FeatureFlagsActivity : BaseActivity<FeatureFlagsViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_flags)

        val featureFlagAdapter = FeatureFlagsAdapter()
        config_flags.adapter = featureFlagAdapter
        config_flags.addItemDecoration(TableItemDecoration())

        this.viewModel.outputs.features()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { featureFlagAdapter.takeFlags(it) }
    }

    private fun displayPreference(@StringRes labelRes: Int, booleanPreferenceType: BooleanPreferenceType, overrideContainer: View) {
        overrideContainer.override_label.setText(labelRes)

        val switch = overrideContainer.override_switch
        switch.isChecked = booleanPreferenceType.get()

        overrideContainer.setOnClickListener {
            booleanPreferenceType.set(BooleanUtils.negate(booleanPreferenceType.get()))
            switch.isChecked = booleanPreferenceType.get()
        }
    }
}
