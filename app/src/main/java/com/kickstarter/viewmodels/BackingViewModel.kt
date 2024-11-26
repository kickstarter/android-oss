package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Backing
import com.kickstarter.models.BackingWrapper
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

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

    class BackingViewModel(environment: Environment, private val intent: Intent? = null) :
        ViewModel(),
        Outputs,
        Inputs {

        private val currentUser: CurrentUserTypeV2
        private val apolloClient: ApolloClientTypeV2
        private val refreshBacking = PublishSubject.create<Unit>()
        private val isRefreshing = PublishSubject.create<Boolean>()
        private val backingWrapper = PublishSubject.create<BackingWrapper>()

        val outputs: Outputs = this
        val inputs: Inputs = this

        private val disposables = CompositeDisposable()
        private fun intent() = intent?.let { Observable.just(it) } ?: Observable.empty()
        init {
            currentUser = requireNotNull(environment.currentUserV2())
            apolloClient = requireNotNull(environment.apolloClientV2())

            val loggedInUser = currentUser.loggedInUser()

            val project = intent()
                .filter { (it.getParcelableExtra<Project>(IntentKey.PROJECT)).isNotNull() }
                .map {
                    it.getParcelableExtra<Project>(IntentKey.PROJECT)
                }
                .map { requireNotNull(it) }

            val backingInfo = intent()
                .filter { (it.getParcelableExtra<Backing>(IntentKey.BACKING)).isNotNull() }
                .map {
                    it.getParcelableExtra<Backing>(IntentKey.BACKING)
                }

            val backing = Observable.combineLatest<Project, Backing?, Pair<Project, Backing>>(
                project,
                backingInfo
            ) { a: Project?, b: Backing? ->
                Pair.create(a, b)
            }
                .switchMap { pb: Pair<Project, Backing> ->
                    apolloClient.getBacking(
                        pb.second.id().toString()
                    )
                }
                .filter { bk: Backing? -> bk.isNotNull() }
                .distinctUntilChanged()
                .compose(Transformers.neverErrorV2())

            backing.compose(Transformers.takeWhenV2(refreshBacking))
                .switchMap {
                    apolloClient.getBacking(
                        it.id().toString()
                    )
                }
                .filter { it.isNotNull() }
                .subscribe { isRefreshing.onNext(false) }
                .addToDisposable(disposables)

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
                .addToDisposable(disposables)
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
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
            refreshBacking.onNext(Unit)
        }

        override fun isRefreshing(): Observable<Boolean> {
            return isRefreshing
        }
    }

    class Factory(private val environment: Environment, private val intent: Intent) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BackingViewModel(environment, intent) as T
        }
    }
}
