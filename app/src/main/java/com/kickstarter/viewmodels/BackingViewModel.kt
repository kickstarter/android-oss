package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Backing
import com.kickstarter.models.BackingWrapper
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.BackingActivity
import rx.Observable
import rx.subjects.PublishSubject

interface BackingViewModel {
    interface Inputs {
        fun refresh()
    }

    interface Outputs {
        /**
         * Set the backer name TextView's text.
         */
        fun showBackingFragment(): Observable<BackingWrapper>
        fun isRefreshing(): Observable<Boolean>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<BackingActivity?>(environment),
        Outputs,
        Inputs {

        private val currentUser: CurrentUserType
        private val apolloClient: ApolloClientType
        private val refreshBacking = PublishSubject.create<Void?>()
        private val isRefreshing = PublishSubject.create<Boolean>()
        private val backingWrapper = PublishSubject.create<BackingWrapper>()

        val outputs: Outputs = this
        val inputs: Inputs = this

        init {
            currentUser = requireNotNull(environment.currentUser())
            apolloClient = requireNotNull(environment.apolloClient())

            val loggedInUser = currentUser.loggedInUser()

            val project = intent()
                .map {
                    it.getParcelableExtra<Project>(IntentKey.PROJECT)
                }.filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            val backingInfo = intent()
                .map {
                    it.getParcelableExtra<Backing>(IntentKey.BACKING)
                }

            val backing = Observable.combineLatest<Project, Backing?, Pair<Project, Backing>>(
                project,
                backingInfo
            ) { a: Project?, b: Backing? -> Pair.create(a, b) }
                .switchMap { pb: Pair<Project, Backing> ->
                    apolloClient.getBacking(
                        pb.second.id().toString()
                    )
                }
                .distinctUntilChanged()
                .filter { bk: Backing? -> ObjectUtils.isNotNull(bk) }
                .compose(Transformers.neverError())

            backing.compose(Transformers.takeWhen(refreshBacking))
                .switchMap {
                    apolloClient.getBacking(
                        it.id().toString()
                    )
                }
                .filter { ObjectUtils.isNotNull(it) }
                .subscribe { isRefreshing.onNext(false) }

            Observable.combineLatest(
                backing,
                project,
                loggedInUser
            ) { backing, project, currentUser ->
                createWrapper(
                    backing,
                    project,
                    currentUser
                )
            }
                .subscribe { v: BackingWrapper -> backingWrapper.onNext(v) }
        }

        private fun createWrapper(
            backing: Backing,
            project: Project,
            currentUser: User
        ): BackingWrapper {
            return BackingWrapper(backing, currentUser, project)
        }

        override fun showBackingFragment(): Observable<BackingWrapper> {
            return backingWrapper
        }

        override fun refresh() {
            refreshBacking.onNext(null)
        }

        override fun isRefreshing(): Observable<Boolean> {
            return isRefreshing
        }
    }
}
