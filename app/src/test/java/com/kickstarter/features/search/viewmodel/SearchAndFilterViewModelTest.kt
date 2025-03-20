package com.kickstarter.features.search.viewmodel

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.search.data.SearchEnvelope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
    fun `test for initial state will only have sorting parameter POPULAR and empty search term`() = runTest {

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
            viewModel.updateSearchTerm("")
            viewModel.searchUIState.toList(searchState)
        }

        advanceUntilIdle()
        assertEquals(params?.sort(), DiscoveryParams.Sort.POPULAR)
        assertNull(params?.term())
        assertEquals(searchState.size, 2)
        assertEquals(searchState.last().popularProjectsList, projectList)
    }

    @Test
    fun `test initial state fails`() = runTest {

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

        var errorNumber = 0
        val searchState = mutableListOf<SearchUIState>()
        backgroundScope.launch(dispatcher) {
            viewModel.provideErrorAction { errorNumber++ }
            viewModel.updateSearchTerm("")
            viewModel.searchUIState.toList(searchState)
        }

        advanceUntilIdle()
        assertEquals(params?.sort(), DiscoveryParams.Sort.POPULAR)
        assertNull(params?.term())
        assertEquals(searchState.size, 2)
        assertEquals(searchState.last().popularProjectsList, emptyList<Project>())
        assertEquals(errorNumber, 1)
    }

    @Test
    fun `test for searching a tem`() = runTest {
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

        var errorNumber = 0
        val searchState = mutableListOf<SearchUIState>()
        backgroundScope.launch(dispatcher) {
            viewModel.provideErrorAction { errorNumber++ }
            viewModel.updateSearchTerm("hello")
            viewModel.searchUIState.toList(searchState)
        }

        advanceUntilIdle()
        assertEquals(params?.sort(), DiscoveryParams.Sort.POPULAR)
        assertEquals(params?.term(), "hello")
        assertEquals(searchState.size, 2)
        assertEquals(searchState.last().popularProjectsList, emptyList<Project>())
        assertEquals(searchState.last().searchList, projectList)
        assertEquals(errorNumber, 0)
    }

    @Test
    fun `test analytics and reftags when from a search with term`() = runTest {
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

        var errorNumber = 0
        val searchState = mutableListOf<SearchUIState>()
        backgroundScope.launch(dispatcher) {
            viewModel.provideErrorAction { errorNumber++ }
            viewModel.updateSearchTerm("hello")
            viewModel.searchUIState.toList(searchState)
        }

        advanceUntilIdle()
        assertEquals(params?.sort(), DiscoveryParams.Sort.POPULAR)
        assertEquals(params?.term(), "hello")
        assertEquals(searchState.size, 2)

        val projAndRefTag1 = viewModel.getProjectAndRefTag(projectList.first())
        val projAndRefTag2 = viewModel.getProjectAndRefTag(projectList.last())

        assertEquals(projAndRefTag1.second, RefTag.searchFeatured())
        assertEquals(projAndRefTag2.second, RefTag.search())

        segmentTrack.assertValues(EventName.CTA_CLICKED.eventName, EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `test analytics and reftags for initial state with empty term and sorting parameter POPULAR`() = runTest {
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

        var errorNumber = 0
        val searchState = mutableListOf<SearchUIState>()
        backgroundScope.launch(dispatcher) {
            viewModel.provideErrorAction { errorNumber++ }
            viewModel.updateSearchTerm("")
            viewModel.searchUIState.toList(searchState)
        }

        advanceUntilIdle()
        assertEquals(params?.sort(), DiscoveryParams.Sort.POPULAR)
        assertNull(params?.term())
        assertEquals(searchState.size, 2)

        val projAndRefTag1 = viewModel.getProjectAndRefTag(projectList.first())
        val projAndRefTag2 = viewModel.getProjectAndRefTag(projectList.last())

        assertEquals(projAndRefTag1.second, RefTag.searchPopularFeatured())
        assertEquals(projAndRefTag2.second, RefTag.searchPopular())

        segmentTrack.assertValues(EventName.CTA_CLICKED.eventName, EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `test for searching a tem with category and sorting`() = runTest {
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

        var errorNumber = 0
        val searchState = mutableListOf<SearchUIState>()
        backgroundScope.launch(dispatcher) {
            viewModel.provideErrorAction { errorNumber++ }
            viewModel.updateSearchTerm("hello")
            viewModel.updateParamsToSearchWith(
                CategoryFactory.gamesCategory(),
                DiscoveryParams.Sort.MOST_FUNDED
            )
            viewModel.updateSearchTerm("cats")
            viewModel.searchUIState.toList(searchState)
        }

        advanceUntilIdle()
        assertEquals(params?.sort(), DiscoveryParams.Sort.MOST_FUNDED)
        assertEquals(params?.category(), CategoryFactory.gamesCategory())
        assertEquals(params?.term(), "cats")
        assertEquals(searchState.size, 2)
        assertEquals(searchState.last().popularProjectsList, emptyList<Project>())
        assertEquals(searchState.last().searchList, projectList)
        assertEquals(errorNumber, 0)
    }

    @Test
    fun `test for searching with clean user selection no category, no sorting, no term options`() = runTest {
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

        var errorNumber = 0
        val searchState = mutableListOf<SearchUIState>()
        backgroundScope.launch(dispatcher) {
            viewModel.provideErrorAction { errorNumber++ }
            viewModel.updateSearchTerm("")
            viewModel.updateParamsToSearchWith(null, DiscoveryParams.Sort.MAGIC)
            viewModel.searchUIState.toList(searchState)
        }

        advanceUntilIdle()
        assertEquals(params?.sort(), DiscoveryParams.Sort.MAGIC)
        assertEquals(params?.category(), null)
        assertEquals(params?.term(), null)
    }

    @Test
    fun `test for searching with clean user selection no category, no sorting, no term options, load two pages`() = runTest {
        var params: DiscoveryParams? = null
        val projectList = listOf(ProjectFactory.project(), ProjectFactory.prelaunchProject(""))
        val secondPageList = listOf(ProjectFactory.caProject(), ProjectFactory.mxProject())
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        var pageCounter = 0
        val environment = environment()
            .toBuilder()
            .apolloClientV2(
                object : MockApolloClientV2() {
                    override suspend fun getSearchProjects(
                        discoveryParams: DiscoveryParams,
                        cursor: String?
                    ): Result<SearchEnvelope> {
                        params = discoveryParams
                        pageCounter++
                        val envelope = if (pageCounter == 2) SearchEnvelope(secondPageList)
                        else SearchEnvelope(projectList)
                        return Result.success(envelope)
                    }
                }).build()

        setUpEnvironment(environment, dispatcher)

        var errorNumber = 0
        val searchState = mutableListOf<SearchUIState>()
        backgroundScope.launch(dispatcher) {
            viewModel.provideErrorAction { errorNumber++ }
            viewModel.updateSearchTerm("")
            viewModel.updateParamsToSearchWith(null, DiscoveryParams.Sort.MAGIC)
            viewModel.loadMore()
            viewModel.searchUIState.toList(searchState)
        }

        advanceUntilIdle()
        assertEquals(params?.sort(), DiscoveryParams.Sort.MAGIC)
        assertEquals(params?.category(), null)
        assertEquals(params?.term(), null)
        assertEquals(searchState.size, 3)
        assertEquals(pageCounter, 2)
    }

    @Test
    fun `test for searching a tem with category and sorting, load two pages`() = runTest {
        var params: DiscoveryParams? = null
        val projectList = listOf(ProjectFactory.project(), ProjectFactory.prelaunchProject(""))

        val secondPageList = listOf(ProjectFactory.caProject(), ProjectFactory.mxProject())
        var pageCounter = 0

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
                        pageCounter++
                        val envelope = if (pageCounter == 2) SearchEnvelope(secondPageList)
                        else SearchEnvelope(projectList)
                        return Result.success(envelope)
                    }
                }).build()

        setUpEnvironment(environment, dispatcher)

        var errorNumber = 0
        val searchState = mutableListOf<SearchUIState>()
        backgroundScope.launch(dispatcher) {
            viewModel.provideErrorAction { errorNumber++ }
            viewModel.updateSearchTerm("hello")
            viewModel.updateParamsToSearchWith(
                CategoryFactory.gamesCategory(),
                DiscoveryParams.Sort.MOST_FUNDED
            )
            viewModel.updateSearchTerm("cats")
            viewModel.loadMore()
            viewModel.searchUIState.toList(searchState)
        }

        advanceUntilIdle()
        assertEquals(params?.sort(), DiscoveryParams.Sort.MOST_FUNDED)
        assertEquals(params?.category(), CategoryFactory.gamesCategory())
        assertEquals(params?.term(), "cats")
        assertEquals(searchState.size, 3)
        assertEquals(pageCounter, 2)
        assertEquals(errorNumber, 0)
    }
}
