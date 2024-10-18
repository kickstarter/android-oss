package com.kickstarter.ui.intents

import android.content.Intent
import android.net.Uri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.DiscoveryParams.Companion.builder
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.intentmappers.DiscoveryIntentMapper.params
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class DiscoveryIntentMapperTest : KSRobolectricTestCase() {
    private val disposables = CompositeDisposable()

    @Test
    fun emitsFromParamsExtra() {
        val params = builder().build()
        val intent = Intent().putExtra(IntentKey.DISCOVERY_PARAMS, params)
        val resultTest = TestSubscriber.create<DiscoveryParams>()

        params(intent, MockApiClientV2(), MockApolloClientV2())
            .subscribe { resultTest.onNext(it) }.addToDisposable(disposables)

        resultTest.assertValues(params)
    }

    @Test
    fun emitsFromDiscoveryUri() {
        val uri = Uri.parse("https://www.kickstarter.com/discover")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val resultTest = TestSubscriber.create<DiscoveryParams>()
        params(intent, MockApiClientV2(), MockApolloClientV2())
            .subscribe { resultTest.onNext(it) }.addToDisposable(disposables)

        resultTest.assertValues(builder().build())
    }

    @Test
    fun emitsFromDiscoveryCategoryUri() {
        val uri = Uri.parse("https://www.kickstarter.com/discover/categories/music")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val resultTest = TestSubscriber.create<DiscoveryParams>()
        params(intent, MockApiClientV2(), MockApolloClientV2())
            .subscribe { resultTest.onNext(it) }.addToDisposable(disposables)

        resultTest.assertValueCount(1)
    }

    @Test
    fun emitsFromDiscoveryLocationUri() {
        val uri = Uri.parse("https://www.kickstarter.com/discover/places/sydney-au")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val resultTest = TestSubscriber.create<DiscoveryParams>()
        params(intent, MockApiClientV2(), MockApolloClientV2())
            .subscribe { resultTest.onNext(it) }.addToDisposable(disposables)

        resultTest.assertValueCount(1)
    }

    @Test
    fun emitsFromAdvancedCategoryIdAndLocationIdUri() {
        val uri =
            Uri.parse("https://www.kickstarter.com/discover/advanced?category_id=1&location_id=1")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val resultTest = TestSubscriber.create<DiscoveryParams>()
        params(intent, MockApiClientV2(), MockApolloClientV2())
            .subscribe { resultTest.onNext(it) }.addToDisposable(disposables)

        resultTest.assertValueCount(1)
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
