package com.kickstarter.viewmodels

import android.content.SharedPreferences
import android.util.Pair
import com.apollographql.apollo.api.CustomTypeValue
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.ApiPaginator
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.graphql.DateTimeAdapter
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isPresent
import com.kickstarter.libs.utils.extensions.isTrimmedEmpty
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.ui.activities.SearchActivity
import com.kickstarter.ui.data.ProjectData.Companion.builder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
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

    class ViewModel(environment: Environment) :
        ActivityViewModel<SearchActivity>(environment),
        Inputs,
        Outputs {
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
            val isFirstResult = selectedProject === projects[0]
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

        private val nextPage = PublishSubject.create<Void?>()
        private val projectClicked = PublishSubject.create<Project>()
        private val search = PublishSubject.create<String>()
        private val isFetchingProjects = BehaviorSubject.create<Boolean>()
        private val popularProjects = BehaviorSubject.create<List<Project>>()
        private val searchProjects = BehaviorSubject.create<List<Project>>()
        private val startProjectActivity = PublishSubject.create<Pair<Project, RefTag>>()
        private val startPreLaunchProjectActivity = PublishSubject.create<Pair<Project, RefTag>>()
        private val optimizely = requireNotNull(environment.optimizely())

        @JvmField
        val inputs: Inputs = this

        @JvmField
        val outputs: Outputs = this

        override fun nextPage() {
            nextPage.onNext(null)
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
            val apiClient = requireNotNull(environment.apiClient())
            val scheduler = environment.scheduler()
            sharedPreferences = requireNotNull(environment.sharedPreferences())
            cookieManager = requireNotNull(environment.cookieManager())

            val searchParams = search
                .filter { ObjectUtils.isNotNull(it) }
                .filter { it.isPresent() }
                .debounce(300, TimeUnit.MILLISECONDS, scheduler)
                .map { DiscoveryParams.builder().term(it).build() }

            val popularParams = search
                .filter { ObjectUtils.isNotNull(it) }
                .filter { it.isTrimmedEmpty() }
                .map { defaultParams }
                .startWith(defaultParams)

            val params = Observable.merge(searchParams, popularParams)

            val paginator = ApiPaginator.builder<Project, DiscoverEnvelope, DiscoveryParams>()
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
                .compose(bindToLifecycle())
                .subscribe(isFetchingProjects)

            search
                .filter { ObjectUtils.isNotNull(it) }
                .filter { it.isTrimmedEmpty() }
                .compose(bindToLifecycle())
                .subscribe { searchProjects.onNext(ListUtils.empty()) }

            params
                .compose(Transformers.takePairWhen(paginator.paginatedData()))
                .compose(bindToLifecycle())
                .subscribe { paramsAndProjects: Pair<DiscoveryParams, List<Project>> ->
                    if (paramsAndProjects.first.sort() == defaultSort) {
                        popularProjects.onNext(paramsAndProjects.second)
                    } else {
                        searchProjects.onNext(paramsAndProjects.second)
                    }
                }

            val pageCount = paginator.loadingPage()
            val projects = Observable.merge(popularProjects, searchProjects)

            params.compose(Transformers.takePairWhen(projectClicked))
                .compose(Transformers.combineLatestPair(pageCount))
                .compose(bindToLifecycle())
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

            val selectedProject =
                Observable.combineLatest<String, List<Project>, Pair<String, List<Project>>>(
                    search,
                    projects
                ) { a: String, b: List<Project> ->
                    Pair.create(a, b)
                }
                    .compose(Transformers.takePairWhen(projectClicked))
                    .map { searchTermAndProjectsAndProjectClicked: Pair<Pair<String, List<Project>>, Project> ->
                        val searchTerm = searchTermAndProjectsAndProjectClicked.first.first
                        val currentProjects = searchTermAndProjectsAndProjectClicked.first.second
                        val projectClicked = searchTermAndProjectsAndProjectClicked.second
                        projectAndRefTag(searchTerm, currentProjects, projectClicked)
                    }

            selectedProject.subscribe {
                if (it.first.launchedAt() == DateTimeAdapter().decode(CustomTypeValue.fromRawValue(0)) &&
                    optimizely.isFeatureEnabled(OptimizelyFeature.Key.ANDROID_PRE_LAUNCH_SCREEN)
                ) {
                    startPreLaunchProjectActivity.onNext(it)
                } else {
                    startProjectActivity.onNext(it)
                }
            }

            params
                .compose(Transformers.takePairWhen(discoverEnvelope))
                .compose(Transformers.combineLatestPair(pageCount))
                .compose(bindToLifecycle())
                .filter { it: Pair<Pair<DiscoveryParams, DiscoverEnvelope>, Int> ->
                    (
                        ObjectUtils.isNotNull(it.first.first.term()) &&
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

            analyticEvents.trackSearchCTAButtonClicked(defaultParams)
        }
    }
}
