package com.kickstarter.viewmodels

import android.content.Intent
import android.content.SharedPreferences
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.loadmore.ApolloPaginateV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.storeCurrentCookieRefTag
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.apiresponses.updatesresponse.UpdatesGraphQlEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.net.CookieManager

interface ProjectUpdatesViewModel {
    interface Inputs {
        /** Call when pagination should happen. */
        fun nextPage()

        /** Call when the feed should be refreshed.  */
        fun refresh()

        /** Call when an Update is clicked.  */
        fun updateClicked(update: Update)
    }

    interface Outputs {
        /** Emits a boolean indicating whether the horizontal ProgressBar is visible.  */
        fun horizontalProgressBarIsGone(): Observable<Boolean>

        /** Emits a boolean indicating whether updates are being fetched from the API.  */
        fun isFetchingUpdates(): Observable<Boolean>

        /** Emits the current project and its updates.  */
        fun projectAndUpdates(): Observable<Pair<Project, List<Update>>>

        /** Emits a project and an update to start the update activity with.  */
        fun startUpdateActivity(): Observable<Pair<Project, Update>>
    }

    class ProjectUpdatesViewModel(environment: Environment, private val intent: Intent? = null) :
        ViewModel(), Inputs, Outputs {
        private val client: ApolloClientTypeV2 = requireNotNull(environment.apolloClientV2())
        private val cookieManager: CookieManager = requireNotNull(environment.cookieManager())
        private val sharedPreferences: SharedPreferences = requireNotNull(environment.sharedPreferences())
        private val analyticEvents = requireNotNull(environment.analytics())
        private val nextPage = PublishSubject.create<Unit>()
        private val refresh = PublishSubject.create<Unit>()
        private val updateClicked = PublishSubject.create<Update>()
        private val horizontalProgressBarIsGone = BehaviorSubject.create<Boolean>()
        private val isFetchingUpdates = PublishSubject.create<Boolean>()
        private val projectAndUpdates = BehaviorSubject.create<Pair<Project, List<Update>>>()
        private val startUpdateActivity = PublishSubject.create<Pair<Project, Update>>()

        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private fun intent() = intent?.let { Observable.just(it) } ?: Observable.empty()
        init {

            val projectData = intent()
                .filter { (it.getParcelableExtra(IntentKey.PROJECT_DATA) as? ProjectData?).isNotNull() }
                .map { it.getParcelableExtra(IntentKey.PROJECT_DATA) as? ProjectData? }
                .filter { it.isNotNull() }
                .take(1)

            val project = projectData
                .filter { it.project().isNotNull() }
                .map { it.project() }

            projectData
                .map {
                    it.storeCurrentCookieRefTag(
                        cookieManager, sharedPreferences
                    )
                }
                .subscribe {
                    analyticEvents.trackProjectScreenViewed(
                        it, EventContextValues.ContextSectionName.UPDATES.contextName
                    )
                }
                .addToDisposable(disposables)

            val startOverWith = Observable.merge(
                project,
                project.compose(Transformers.takeWhenV2(refresh))
            )

            val paginator =
                ApolloPaginateV2.builder<Update, UpdatesGraphQlEnvelope, Project>()
                    .nextPage(nextPage)
                    .distinctUntilChanged(true)
                    .startOverWith(startOverWith)
                    .envelopeToListOfData {
                        it.updates?.filter { updates ->
                            !updates.body().isNullOrEmpty()
                        }
                    }
                    .loadWithParams {
                        loadWithProjectUpdatesList(Observable.just(it.first), it.second)
                    }
                    .clearWhenStartingOver(false)
                    .build()

            paginator.paginatedData()
                ?.share()
                ?.let {
                    project
                        .compose<Pair<Project, List<Update>>>(Transformers.combineLatestPair(it))
                        .subscribe { projectAndUpdates.onNext(it) }
                        .addToDisposable(disposables)
                }

            paginator
                .isFetching()
                .subscribe {
                    horizontalProgressBarIsGone.onNext(!it)
                }
                .addToDisposable(disposables)

            paginator
                .isFetching()
                .subscribe {
                    isFetchingUpdates.onNext(it)
                }
                .addToDisposable(disposables)

            project
                .compose(Transformers.takePairWhenV2(updateClicked))
                .subscribe { startUpdateActivity.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
        private fun loadWithProjectUpdatesList(
            project: Observable<Project>,
            cursor: String?
        ): Observable<UpdatesGraphQlEnvelope> {
            return project.switchMap {
                // TODO: review the limit ... how is it possible it was not requested before
                return@switchMap client.getProjectUpdates(it.slug() ?: "", cursor, 2)
            }.onErrorResumeNext(Observable.empty())
        }
        override fun nextPage() {
            nextPage.onNext(Unit)
        }

        override fun refresh() {
            refresh.onNext(Unit)
        }

        override fun updateClicked(update: Update) {
            updateClicked.onNext(update)
        }

        override fun horizontalProgressBarIsGone(): Observable<Boolean> = horizontalProgressBarIsGone
        override fun isFetchingUpdates(): Observable<Boolean> = isFetchingUpdates
        override fun projectAndUpdates(): Observable<Pair<Project, List<Update>>> = projectAndUpdates
        override fun startUpdateActivity(): Observable<Pair<Project, Update>> = startUpdateActivity
    }

    class Factory(private val environment: Environment, private val intent: Intent) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectUpdatesViewModel(environment, intent) as T
        }
    }
}
