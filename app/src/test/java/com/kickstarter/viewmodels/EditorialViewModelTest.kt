package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Category
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.Editorial
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class EditorialViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: EditorialViewModel.EditorialViewModel

    private val description = TestSubscriber<Int>()
    private val discoveryParams = TestSubscriber<DiscoveryParams>()
    private val graphic = TestSubscriber<Int>()
    private val refreshDiscoveryFragment = TestSubscriber<Unit>()
    private val retryContainerIsGone = TestSubscriber<Boolean>()
    private val rootCategories = TestSubscriber<List<Category>>()
    private val title = TestSubscriber<Int>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment, editorial: Editorial) {
        this.vm = EditorialViewModel.EditorialViewModel(
            environment,
            Intent().putExtra(IntentKey.EDITORIAL, editorial)
        )

        this.vm.outputs.description().subscribe { this.description.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.discoveryParams().subscribe { this.discoveryParams.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.graphic().subscribe { this.graphic.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.refreshDiscoveryFragment()
            .subscribe { this.refreshDiscoveryFragment.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.retryContainerIsGone().subscribe { this.retryContainerIsGone.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.rootCategories().subscribe { this.rootCategories.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.title().subscribe { this.title.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testDescription() {
        setUpEnvironment(environment(), Editorial.GO_REWARDLESS)

        this.description.assertValue(R.string.These_projects_could_use_your_support)
    }

    @Test
    fun testDiscoveryParams_whenGoRewardless() {
        setUpEnvironment(environment(), Editorial.GO_REWARDLESS)

        val expectedParams = DiscoveryParams.builder()
            .sort(DiscoveryParams.Sort.MAGIC)
            .tagId(Editorial.GO_REWARDLESS.tagId)
            .build()
        this.discoveryParams.assertValue(expectedParams)
    }

    @Test
    fun testDiscoveryParams_whenLightsOn() {
        setUpEnvironment(environment(), Editorial.LIGHTS_ON)

        val expectedParams = DiscoveryParams.builder()
            .sort(DiscoveryParams.Sort.MAGIC)
            .tagId(557)
            .build()
        this.discoveryParams.assertValue(expectedParams)
    }

    @Test
    fun testGraphic() {
        setUpEnvironment(environment(), Editorial.GO_REWARDLESS)

        this.graphic.assertValue(R.drawable.go_rewardless_header)
    }

    @Test
    fun testRefreshDiscoveryFragment() {
        setUpEnvironment(environment(), Editorial.GO_REWARDLESS)

        this.vm.inputs.retryContainerClicked()

        this.refreshDiscoveryFragment.assertValueCount(1)
    }

    @Test
    fun testRetryContainerIsGone() {
        var error = true
        val environment = environment()
            .toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun fetchCategories(): Observable<List<Category>> {
                    return when {
                        error -> Observable.error(Throwable("boop"))
                        else -> {
                            super.fetchCategories()
                        }
                    }
                }
            })
            .build()
        setUpEnvironment(environment, Editorial.GO_REWARDLESS)

        this.retryContainerIsGone.assertValues(false)

        error = false
        this.vm.inputs.retryContainerClicked()

        this.retryContainerIsGone.assertValues(false, true, true)
    }

    @Test
    fun testRootCategories() {
        val environment = environment()
            .toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun fetchCategories(): Observable<List<Category>> {
                    return Observable.just(
                        listOf(
                            CategoryFactory.artCategory(),
                            CategoryFactory.ceramicsCategory(),
                            CategoryFactory.gamesCategory()
                        )
                    )
                }
            })
            .build()
        setUpEnvironment(environment, Editorial.GO_REWARDLESS)

        this.rootCategories.assertValue(
            listOf(
                CategoryFactory.artCategory(),
                CategoryFactory.gamesCategory()
            )
        )
    }

    @Test
    fun testTitle() {
        setUpEnvironment(environment(), Editorial.GO_REWARDLESS)

        this.title.assertValue(R.string.This_holiday_season_support_a_project_for_no_reward)
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
