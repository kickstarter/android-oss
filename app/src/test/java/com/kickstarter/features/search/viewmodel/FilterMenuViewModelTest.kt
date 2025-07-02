package com.kickstarter.features.search.viewmodel

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FilterMenuViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: FilterMenuViewModel

    private fun setUpEnvironment(environment: Environment, dispatcher: CoroutineDispatcher) {
        viewModel = FilterMenuViewModel.Factory(environment, dispatcher)
            .create(FilterMenuViewModel::class.java)
    }

    @Test
    fun `test obtain rootCategories succeed`() = runTest {

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val environment = environment()
            .toBuilder()
            .apolloClientV2(
                object : MockApolloClientV2() {
                    override suspend fun getCategories(): Result<List<Category>> {
                        return Result.success(CategoryFactory.rootCategories())
                    }
                }).build()

        setUpEnvironment(environment, dispatcher)

        var errorCounter = 0
        val state = mutableListOf<FilterMenuUIState>()
        backgroundScope.launch(dispatcher) {
            viewModel.provideErrorAction { errorCounter++ }
            viewModel.getRootCategories()
            viewModel.filterMenuUIState.toList(state)
        }

        advanceUntilIdle()
        assertEquals(errorCounter, 0)
        assertEquals(state.size, 2)
        assertEquals(state.first().categoriesList, emptyList<Category>())
        assertEquals(state.last().categoriesList, CategoryFactory.rootCategories())
    }

    @Test
    fun `test obtain rootCategories errored`() = runTest {

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val environment = environment()
            .toBuilder()
            .apolloClientV2(
                object : MockApolloClientV2() {
                    override suspend fun getCategories(): Result<List<Category>> {
                        return Result.failure(Exception())
                    }
                }).build()

        setUpEnvironment(environment, dispatcher)

        var errorCounter = 0
        val state = mutableListOf<FilterMenuUIState>()
        backgroundScope.launch(dispatcher) {
            viewModel.provideErrorAction { errorCounter++ }
            viewModel.getRootCategories()
            viewModel.filterMenuUIState.toList(state)
        }

        advanceUntilIdle()
        assertEquals(errorCounter, 1)
        assertEquals(state.size, 1)
        assertEquals(state.first().categoriesList, emptyList<Category>())
    }

    @Test
    fun `Default locations loaded on init, and search query triggers getLocations and updates searchedLocations`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val searched = listOf(LocationFactory.vancouver())
        val default = listOf(LocationFactory.unitedStates())

        var useDefaultParam = mutableListOf<Boolean>()
        var termParam = mutableListOf<String?>()
        var timesCalled = 0
        val environment = environment()
            .toBuilder()
            .apolloClientV2(
                object : MockApolloClientV2() {
                    override suspend fun getLocations(
                        useDefault: Boolean,
                        term: String?,
                        lat: Float?,
                        long: Float?,
                        radius: Float?,
                        filterByCoordinates: Boolean?
                    ): Result<List<Location>> {
                        termParam.add(term)
                        useDefaultParam.add(useDefault)
                        timesCalled++
                        return if (!useDefault && term == "Vancouver") {
                            Result.success(searched)
                        } else {
                            Result.success(default)
                        }
                    }
                }).build()

        val state = mutableListOf<LocationsUIState>()
        backgroundScope.launch(dispatcher) {
            setUpEnvironment(environment, dispatcher)
            viewModel.updateQuery("Vancouver")
            viewModel.locationsUIState.toList(state)
        }
        advanceUntilIdle()

        assertEquals(timesCalled, 2)
        assertNull(termParam.first()) // - Default locations = null term
        assertEquals(termParam.last(), "Vancouver")
        assertTrue(useDefaultParam.first().isTrue())
        //assertFalse(useDefaultParam.last().isFalse())
        assertEquals(state.size, 3)
        assertEquals(state.last().nearLocations.last(), default.last())
        assertEquals(state.last().searchedLocations.last(), searched.last())
    }
}
