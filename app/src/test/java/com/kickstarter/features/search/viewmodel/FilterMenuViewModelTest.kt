package com.kickstarter.features.search.viewmodel

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Category
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
                    override suspend fun getRootCategories(): Result<List<Category>> {
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
                    override suspend fun getRootCategories(): Result<List<Category>> {
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
}
