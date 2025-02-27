package com.kickstarter.features.search.viewmodel

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.search.data.SearchEnvelope
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchAndFilterViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: SearchAndFilterViewModel

    private fun setUpEnvironment(environment: Environment, dispatcher: CoroutineDispatcher) {
        viewModel = SearchAndFilterViewModel.Factory(environment, dispatcher)
            .create(SearchAndFilterViewModel::class.java)
    }

    @Test
    fun `test for initial state getPopularProjects will only have sorting parameter POPULAR`() = runTest {

        var params: DiscoveryParams? = null
        val projectList = listOf(ProjectFactory.project(), ProjectFactory.prelaunchProject(""))
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val environment = environment()
            .toBuilder()
            .apolloClientV2(
                object : MockApolloClientV2() {
                    override suspend fun getSearchProjects(
                        discoveryParams: DiscoveryParams,
                        cursor: String?
                    ): Result<SearchEnvelope> {
                        params = discoveryParams
                        return Result.success(SearchEnvelope(projectList))
                    }
                }).build()

        setUpEnvironment(environment, dispatcher)

        val searchState = mutableListOf<SearchUIState>()
        backgroundScope.launch(dispatcher) {
            viewModel.getPopularProjects()
            viewModel.searchUIState.toList(searchState)
        }

        assertEquals(params?.sort(), DiscoveryParams.Sort.POPULAR)
        assertEquals(searchState.size, 2)
        assertEquals(searchState.last().popularProjectsList, projectList)
    }

    @Test
    fun `test getPopularProjects fails`() = runTest {

        var params: DiscoveryParams? = null
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val environment = environment()
            .toBuilder()
            .apolloClientV2(
                object : MockApolloClientV2() {
                    override suspend fun getSearchProjects(
                        discoveryParams: DiscoveryParams,
                        cursor: String?
                    ): Result<SearchEnvelope> {
                        params = discoveryParams
                        return Result.failure(Exception())
                    }
                }).build()

        setUpEnvironment(environment, dispatcher)

        val searchState = mutableListOf<SearchUIState>()
        backgroundScope.launch(dispatcher) {
            viewModel.getPopularProjects()
            viewModel.searchUIState.toList(searchState)
        }

        assertEquals(params?.sort(), DiscoveryParams.Sort.POPULAR)
        assertEquals(searchState.size, 2)
        assertEquals(searchState.last().popularProjectsList, emptyList<Project>())
        assertEquals(searchState.last().isErrored, true)
    }
}
