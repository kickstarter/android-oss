package com.kickstarter.viewmodels

import android.content.Intent
import android.content.SharedPreferences
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.ApiPaginatorV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isPresent
import com.kickstarter.libs.utils.extensions.isTrimmedEmpty
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.ui.data.ProjectData.Companion.builder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import java.net.CookieManager
import java.util.concurrent.TimeUnit

interface SearchViewModel {
    interface Inputs {
        /** Call when the next page has been invoked.  */
        fun nextPage()

        /** Call when a project is tapped in search results.  */
        fun projectClicked(project: Project)

        /** Call when text changes in search box.  */
        fun search(s: String)
    }

    interface Outputs {
        /** Emits a boolean indicating whether projects are being fetched from the API.  */
        fun isFetchingProjects(): Observable<Boolean>

        /** Emits list of popular projects.  */
        fun popularProjects(): Observable<List<Project>>

        /** Emits list of projects matching criteria.  */
        fun searchProjects(): Observable<List<Project>>

        /** Emits a project and ref tag when we should start a project activity.  */
        fun startProjectActivity(): Observable<Pair<Project, RefTag>>

        /** Emits a Project and RefTag pair when we should start the [com.kickstarter.ui.activities.PreLaunchProjectPageActivity].  */
        fun startPreLaunchProjectActivity(): Observable<Pair<Project, RefTag>>
    }

    class SearchViewModel(
        private val environment: Environment,
        private val intent: Intent? = null
    ) : ViewModel(), Inputs, Outputs {
        private val discoverEnvelope = PublishSubject.create<DiscoverEnvelope>()
        private val sharedPreferences: SharedPreferences
        private val cookieManager: CookieManager

        /**
         * Returns a project and its appropriate ref tag given its location in a list of popular projects or search results.
         *
         * @param searchTerm        The search term entered to determine list of search results.
         * @param projects          The list of popular or search result projects.
         * @param selectedProject   The project selected by the user.
         * @return The project and its appropriate ref tag.
         */
        private fun projectAndRefTag(
            searchTerm: String,
            projects: List<Project>,
            selectedProject: Project
        ): Pair<Project, RefTag> {
            val isFirstResult = if (projects.isEmpty()) false else selectedProject === projects[0]
            return if (searchTerm.isEmpty()) {
                if (isFirstResult) Pair.create(
                    selectedProject,
                    RefTag.searchPopularFeatured()
                ) else Pair.create(selectedProject, RefTag.searchPopular())
            } else {
                if (isFirstResult) Pair.create(
                    selectedProject,
                    RefTag.searchFeatured()
                ) else Pair.create(selectedProject, RefTag.search())
            }
        }

        private val nextPage = PublishSubject.create<Unit>()
        private val projectClicked = PublishSubject.create<Project>()
        private val search = PublishSubject.create<String>()
        private val isFetchingProjects = BehaviorSubject.create<Boolean>()
        private val popularProjects = BehaviorSubject.create<List<Project>>()
        private val searchProjects = BehaviorSubject.create<List<Project>>()
        private val startProjectActivity = PublishSubject.create<Pair<Project, RefTag>>()
        private val startPreLaunchProjectActivity = PublishSubject.create<Pair<Project, RefTag>>()
        private val ffClient = requireNotNull(environment.featureFlagClient())
        private val disposables = CompositeDisposable()

        @JvmField
        val inputs: Inputs = this

        @JvmField
        val outputs: Outputs = this

        fun clearSearchedProjects() {
            searchProjects.onNext(listOf())
        }

        override fun nextPage() {
            nextPage.onNext(Unit)
        }

        override fun projectClicked(project: Project) {
            projectClicked.onNext(project)
        }

        override fun search(s: String) {
            search.onNext(s)
        }

        override fun startProjectActivity(): Observable<Pair<Project, RefTag>> {
            return startProjectActivity
        }

        override fun startPreLaunchProjectActivity(): Observable<Pair<Project, RefTag>> {
            return startPreLaunchProjectActivity
        }

        override fun isFetchingProjects(): Observable<Boolean> {
            return isFetchingProjects
        }

        fun setIsFetching(fetching: Boolean) {
            isFetchingProjects.onNext(fetching)
        }

        override fun popularProjects(): Observable<List<Project>> {
            return popularProjects
        }

        override fun searchProjects(): Observable<List<Project>> {
            return searchProjects
        }

        companion object {
            private val defaultSort = DiscoveryParams.Sort.POPULAR
            private val defaultParams = DiscoveryParams.builder().sort(defaultSort).build()
        }

        init {
            val apiClient = requireNotNull(environment.apiClientV2())
            val scheduler = environment.schedulerV2()
            val analyticEvents = requireNotNull(environment.analytics())
            sharedPreferences = requireNotNull(environment.sharedPreferences())
            cookieManager = requireNotNull(environment.cookieManager())

            val searchParams = search
                .filter { it.isNotNull() }
                .filter { it.isPresent() }
                .debounce(300, TimeUnit.MILLISECONDS, scheduler)
                .map { DiscoveryParams.builder().term(it).build() }

            val popularParams = search
                .filter { it.isNotNull() }
                .filter { it.isTrimmedEmpty() }
                .map { defaultParams }
                .startWith(defaultParams)

            val params = Observable.merge(searchParams, popularParams)

            val paginator = ApiPaginatorV2.builder<Project, DiscoverEnvelope, DiscoveryParams>()
                .nextPage(nextPage)
                .startOverWith(params)
                .envelopeToListOfData { envelope: DiscoverEnvelope ->
                    discoverEnvelope.onNext(envelope)
                    envelope.projects()
                }
                .envelopeToMoreUrl { env: DiscoverEnvelope ->
                    env.urls()?.api()?.moreProjects()
                }
                .clearWhenStartingOver(true)
                .concater { xs: List<Project>, ys: List<Project> ->
                    ListUtils.concatDistinct(
                        xs,
                        ys
                    )
                }
                .loadWithParams {
                    apiClient.fetchProjects(it)
                }
                .loadWithPaginationPath {
                    apiClient.fetchProjects(it)
                }
                .build()

            paginator.isFetching
                .subscribe(isFetchingProjects)

            search
                .filter { it.isNotNull() }
                .filter { it.isTrimmedEmpty() }
                .subscribe { searchProjects.onNext(ListUtils.empty()) }
                .addToDisposable(disposables)

            params
                .compose(Transformers.takePairWhenV2(paginator.paginatedData()))
                .subscribe { paramsAndProjects: Pair<DiscoveryParams, List<Project>> ->
                    if (paramsAndProjects.first.sort() == defaultSort) {
                        popularProjects.onNext(paramsAndProjects.second)
                    } else {
                        searchProjects.onNext(paramsAndProjects.second)
                    }
                }
                .addToDisposable(disposables)

            val pageCount = paginator.loadingPage()
            val projects = Observable.merge(popularProjects, searchProjects)

            params.compose(Transformers.takePairWhenV2(projectClicked))
                .compose(Transformers.combineLatestPair(pageCount))
                .subscribe { projectDiscoveryParamsPair: Pair<Pair<DiscoveryParams, Project>, Int> ->
                    val refTag = RefTagUtils.projectAndRefTagFromParamsAndProject(
                        projectDiscoveryParamsPair.first.first,
                        projectDiscoveryParamsPair.first.second
                    )
                    val cookieRefTag = RefTagUtils.storedCookieRefTagForProject(
                        projectDiscoveryParamsPair.first.second,
                        cookieManager,
                        sharedPreferences
                    )
                    val projectData = builder()
                        .refTagFromIntent(refTag.second)
                        .refTagFromCookie(cookieRefTag)
                        .project(projectDiscoveryParamsPair.first.second)
                        .build()

                    analyticEvents.trackDiscoverSearchResultProjectCATClicked(
                        projectDiscoveryParamsPair.first.first,
                        projectData,
                        projectDiscoveryParamsPair.second,
                        defaultSort
                    )
                }
                .addToDisposable(disposables)

            val selectedProject =
                Observable.combineLatest<String, List<Project>, Pair<String, List<Project>>>(
                    search,
                    projects
                ) { a: String, b: List<Project> ->
                    Pair.create(a, b)
                }
                    .compose(Transformers.takePairWhenV2(projectClicked))
                    .map { searchTermAndProjectsAndProjectClicked: Pair<Pair<String, List<Project>>, Project> ->
                        val searchTerm = searchTermAndProjectsAndProjectClicked.first.first
                        val currentProjects = searchTermAndProjectsAndProjectClicked.first.second
                        val projectClicked = searchTermAndProjectsAndProjectClicked.second
                        projectAndRefTag(searchTerm, currentProjects, projectClicked)
                    }

            selectedProject.subscribe {
                val epochDateTime = DateTime(0)
                if (it.first.launchedAt() == epochDateTime &&
                    ffClient.getBoolean(FlagKey.ANDROID_PRE_LAUNCH_SCREEN)
                ) {
                    startPreLaunchProjectActivity.onNext(it)
                } else {
                    startProjectActivity.onNext(it)
                }
            }
                .addToDisposable(disposables)

            params
                .compose(Transformers.takePairWhenV2(discoverEnvelope))
                .compose(Transformers.combineLatestPair(pageCount))
                .filter { it: Pair<Pair<DiscoveryParams, DiscoverEnvelope>, Int> ->
                    (
                        it.first.first.term().isNotNull() &&
                            it.first.first.term()?.isPresent() ?: false &&
                            it.first.first.sort() != defaultSort &&
                            it.second.intValueOrZero() == 1
                        )
                }
                .distinct()
                .subscribe { it: Pair<Pair<DiscoveryParams, DiscoverEnvelope>, Int> ->
                    analyticEvents.trackSearchResultPageViewed(
                        it.first.first,
                        it.first.second.stats()?.count() ?: 0,
                        defaultSort
                    )
                }
                .addToDisposable(disposables)

            analyticEvents.trackSearchCTAButtonClicked(defaultParams)
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment, private val intent: Intent? = null) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(environment, intent) as T
        }
    }
}
