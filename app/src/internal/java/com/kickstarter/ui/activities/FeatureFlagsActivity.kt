package com.kickstarter.ui.activities

import android.os.Bundle
import com.kickstarter.KSApplication
import com.kickstarter.databinding.ActivityFeatureFlagsBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ConfigFeatureFlagName.SEGMENT_ENABLED
import com.kickstarter.ui.adapters.FeatureFlagsAdapter
import com.kickstarter.ui.itemdecorations.TableItemDecoration
import com.kickstarter.ui.viewholders.FeatureFlagViewHolder
import com.kickstarter.viewmodels.FeatureFlagsViewModel
import javax.inject.Inject

@RequiresActivityViewModel(FeatureFlagsViewModel.ViewModel::class)
class FeatureFlagsActivity : BaseActivity<FeatureFlagsViewModel.ViewModel>(), FeatureFlagViewHolder.Delegate {

    @JvmField
    @Inject
    var featuresFlagPreference: StringPreferenceType? = null

    private lateinit var binding: ActivityFeatureFlagsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeatureFlagsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (applicationContext as KSApplication).component().inject(this)

        val configFlagsAdapter = FeatureFlagsAdapter(this)
        binding.configFlags.adapter = configFlagsAdapter
        binding.configFlags.addItemDecoration(TableItemDecoration())

        val optimizelyFlagsAdapter = FeatureFlagsAdapter(this)
        binding.optimizelyFlags.adapter = optimizelyFlagsAdapter
        binding.optimizelyFlags.addItemDecoration(TableItemDecoration())

        this.viewModel.outputs.configFeatures()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { configFlagsAdapter.takeFlags(it) }

        this.viewModel.outputs.optimizelyFeatures()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { optimizelyFlagsAdapter.takeFlags(it) }
    }

    override fun featureOptionToggle(featureName: String, isEnabled: Boolean) {

        when (featureName) {
            SEGMENT_ENABLED.featureFlag -> {
                this.viewModel.inputs.updateSegmentFlag(isEnabled, featuresFlagPreference)
            }
        }
    }
}
