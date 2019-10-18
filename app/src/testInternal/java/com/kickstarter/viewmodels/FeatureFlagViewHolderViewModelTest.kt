package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import org.junit.Test
import rx.observers.TestSubscriber

class FeatureFlagViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: FeatureFlagViewHolderViewModel.ViewModel

    private val key = TestSubscriber<String>()
    private val value = TestSubscriber<String>()
    private val valueTextColor = TestSubscriber<Int>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = FeatureFlagViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.key().subscribe(this.key)
        this.vm.outputs.value().subscribe(this.value)
        this.vm.outputs.valueTextColor().subscribe(this.valueTextColor)
    }

    @Test
    fun testKey() {
        setUpEnvironment(environment())

        this.vm.inputs.featureFlag(Pair("key", true))

        this.key.assertValue("key")
    }

    @Test
    fun testValue() {
        setUpEnvironment(environment())

        this.vm.inputs.featureFlag(Pair("key", true))

        this.value.assertValue("true")
    }

    @Test
    fun testValueTextColor_whenFeatureEnabled() {
        setUpEnvironment(environment())

        this.vm.inputs.featureFlag(Pair("key", true))

        this.valueTextColor.assertValue(R.color.text_primary)
    }

    @Test
    fun testValueTextColor_whenFeatureDisabled() {
        setUpEnvironment(environment())

        this.vm.inputs.featureFlag(Pair("key", false))

        this.valueTextColor.assertValue(R.color.text_secondary)
    }
}
