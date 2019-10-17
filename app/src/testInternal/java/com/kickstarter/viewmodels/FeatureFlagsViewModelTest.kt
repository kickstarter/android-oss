package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import org.junit.Test
import rx.observers.TestSubscriber

class FeatureFlagsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: FeatureFlagsViewModel.ViewModel

    private val features = TestSubscriber<List<Pair<String, Boolean>>>()

    private fun setUpEnvironment(features: Map<String, Boolean>?) {
        val mockConfig = MockCurrentConfig()
        mockConfig.config(ConfigFactory.config().toBuilder()
                .features(features)
                .build())
        val environment = environment().toBuilder().currentConfig(mockConfig).build()
        this.vm = FeatureFlagsViewModel.ViewModel(environment)

        this.vm.outputs.features().subscribe(this.features)
    }

    @Test
    fun testFeatures_whenNull() {
        setUpEnvironment(null)

        this.features.assertValue(listOf())
    }

    @Test
    fun testFeatures() {
        val features = mapOf(Pair("ios_feature_one", true),
                Pair("ios_feature_two", false),
                Pair("android_feature_one", true),
                Pair("android_feature_two", false))
        setUpEnvironment(features)

        this.features.assertValue(listOf(Pair("android_feature_one", true),
                Pair("android_feature_two", false)))
    }
}
