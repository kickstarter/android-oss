package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.model.FeatureFlagsModel
import org.junit.Test
import rx.observers.TestSubscriber

class FeatureFlagViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: FeatureFlagViewHolderViewModel.ViewModel

    private val key = TestSubscriber<String>()
    private val value = TestSubscriber<Boolean>()
    private val isClickable = TestSubscriber<Boolean>()
    private val featureAlpha = TestSubscriber<Float>()
    private val notifyDelegateFeatureStateChanged = TestSubscriber<Pair<String, Boolean>>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = FeatureFlagViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.key().subscribe(this.key)
        this.vm.outputs.value().subscribe(this.value)
        this.vm.outputs.isClickable().subscribe(this.isClickable)
        this.vm.outputs.featureAlpha().subscribe(this.featureAlpha)
        this.vm.notifyDelegateFeatureStateChanged().subscribe(this.notifyDelegateFeatureStateChanged)
    }

    @Test
    fun testKey() {
        setUpEnvironment(environment())

        this.vm.inputs.featureFlag(
            FeatureFlagsModel(
                "key",
                isFeatureFlagEnabled = true,
                isFeatureFlagChangeable = true
            )
        )

        this.key.assertValue("key")
    }

    @Test
    fun testValue() {
        setUpEnvironment(environment())

        this.vm.inputs.featureFlag(
            FeatureFlagsModel(
                "key",
                isFeatureFlagEnabled = true,
                isFeatureFlagChangeable = true
            )
        )

        this.value.assertValue(true)
    }

    @Test
    fun testAlphaValue_whenFeatureIsChangeable() {
        setUpEnvironment(environment())

        this.vm.inputs.featureFlag(
            FeatureFlagsModel(
                "key",
                isFeatureFlagEnabled = true,
                isFeatureFlagChangeable = true
            )
        )

        this.isClickable.assertValue(true)
        this.featureAlpha.assertValue(1f)
    }

    @Test
    fun testValueTextColor_whenFeatureIsNotChangeable() {
        setUpEnvironment(environment())

        this.vm.inputs.featureFlag(
            FeatureFlagsModel(
                "key",
                isFeatureFlagEnabled = true,
                isFeatureFlagChangeable = false
            )
        )

        this.isClickable.assertValue(false)
        this.featureAlpha.assertValue(0.5f)
    }

    @Test
    fun testNotifyDelegateFeatureStateChanged() {
        setUpEnvironment(environment())

        this.vm.inputs.featureFlag(
            FeatureFlagsModel(
                "key",
                isFeatureFlagEnabled = true,
                isFeatureFlagChangeable = false
            )
        )

        this.vm.inputs.featureFlagCheckedChange(false)

        this.notifyDelegateFeatureStateChanged.assertValue(Pair("key", false))
    }
}
