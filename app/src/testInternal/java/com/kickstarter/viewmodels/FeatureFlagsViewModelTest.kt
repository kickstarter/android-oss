package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.MockExperimentsClientType
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.models.User
import org.junit.Test
import rx.observers.TestSubscriber

class FeatureFlagsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: FeatureFlagsViewModel.ViewModel

    private val configFeatures = TestSubscriber<List<Pair<String, Boolean>>>()
    private val optimizelyFeatures = TestSubscriber<List<Pair<String, Boolean>>>()

    private fun setUpEnvironment(features: Map<String, Boolean>?, optimizelyFeatures: List<String>) {
        val mockConfig = MockCurrentConfig()
        mockConfig.config(
            ConfigFactory.config().toBuilder()
                .features(features)
                .build()
        )
        val environment = environment()
            .toBuilder()
            .currentConfig(mockConfig)
            .optimizely(object : MockExperimentsClientType() {
                override fun enabledFeatures(user: User?): List<String> {
                    return optimizelyFeatures
                }
            })
            .build()
        this.vm = FeatureFlagsViewModel.ViewModel(environment)

        this.vm.outputs.configFeatures().subscribe(this.configFeatures)
        this.vm.outputs.optimizelyFeatures().subscribe(this.optimizelyFeatures)
    }

    @Test
    fun testConfigFeatures_whenFeaturesMapIsNull() {
        setUpEnvironment(null, emptyList())

        this.configFeatures.assertValue(listOf())
    }

    @Test
    fun testConfigFeatures_whenFeaturesMapIsNotNull() {
        val features = mapOf(
            Pair("ios_feature_one", true),
            Pair("ios_feature_two", false),
            Pair("android_feature_one", true),
            Pair("android_feature_two", false)
        )
        setUpEnvironment(features, emptyList())

        this.configFeatures.assertValue(
            listOf(
                Pair("android_feature_one", true),
                Pair("android_feature_two", false)
            )
        )
    }

    @Test
    fun testOptimizelyFeatures() {
        setUpEnvironment(null, listOf("android_optimizely_feature"))

        this.optimizelyFeatures.assertValue(listOf(Pair("android_optimizely_feature", true)))
    }
}
