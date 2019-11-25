package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.Category
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.Editorial
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class EditorialViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: EditorialViewModel.ViewModel

    private val description = TestSubscriber<Int>()
    private val discoveryParams = TestSubscriber<DiscoveryParams>()
    private val graphic = TestSubscriber<Int>()
    private val rootCategories = TestSubscriber<List<Category>>()
    private val title = TestSubscriber<Int>()

    private fun setUpEnvironment(environment: Environment, editorial: Editorial) {
        this.vm = EditorialViewModel.ViewModel(environment)

        this.vm.outputs.description().subscribe(this.description)
        this.vm.outputs.discoveryParams().subscribe(this.discoveryParams)
        this.vm.outputs.graphic().subscribe(this.graphic)
        this.vm.outputs.rootCategories().subscribe(this.rootCategories)
        this.vm.outputs.title().subscribe(this.title)

        this.vm.intent(Intent().putExtra(IntentKey.EDITORIAL, editorial))
    }

    @Test
    fun testDescription() {
        setUpEnvironment(environment(), Editorial.GO_REWARDLESS)

        this.description.assertValue(R.string.These_projects_could_use_your_support)
    }

    @Test
    fun testDiscoveryParams() {
        setUpEnvironment(environment(), Editorial.GO_REWARDLESS)

        val expectedParams = DiscoveryParams.builder()
                .sort(DiscoveryParams.Sort.HOME)
                .tagId(Editorial.GO_REWARDLESS.tagId)
                .build()
        this.discoveryParams.assertValue(expectedParams)
    }

    @Test
    fun testGraphic() {
        setUpEnvironment(environment(), Editorial.GO_REWARDLESS)

        this.graphic.assertValue(R.drawable.go_rewardless_header)
    }

    @Test
    fun testRootCategories() {
        val environment = environment()
                .toBuilder()
                .apiClient(object : MockApiClient() {
                    override fun fetchCategories(): Observable<List<Category>> {
                        return Observable.just(listOf(CategoryFactory.artCategory(),
                                CategoryFactory.ceramicsCategory(),
                                CategoryFactory.gamesCategory()))
                    }
                })
                .build()
        setUpEnvironment(environment, Editorial.GO_REWARDLESS)

        this.rootCategories.assertValue(listOf(CategoryFactory.artCategory(),
                CategoryFactory.gamesCategory()))
    }

    @Test
    fun testTitle() {
        setUpEnvironment(environment(), Editorial.GO_REWARDLESS)

        this.title.assertValue(R.string.This_holiday_season_support_a_project_for_no_reward)
    }
}
