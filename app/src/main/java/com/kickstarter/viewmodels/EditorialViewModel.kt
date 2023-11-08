package com.kickstarter.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Category
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.Editorial
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface EditorialViewModel {
    interface Inputs {
        /** Call when the user clicks the retry container. */
        fun retryContainerClicked()
    }

    interface Outputs {
        /** Emits the @StringRes ID of the description of the [Editorial]. */
        fun description(): Observable<Int>

        /** Emits the [DiscoveryParams] for the tag of the [Editorial]. */
        fun discoveryParams(): Observable<DiscoveryParams>

        /** Emits the @DrawableRes of the graphic of the [Editorial]. */
        fun graphic(): Observable<Int>

        /** Emits when we should refresh the [com.kickstarter.ui.fragments.DiscoveryFragment]. */
        fun refreshDiscoveryFragment(): Observable<Unit>

        /** Emits a [Boolean] determining if the retry container should be visible. */
        fun retryContainerIsGone(): Observable<Boolean>

        /** Emits a list of root [Category]s. */
        fun rootCategories(): Observable<List<Category>>

        /** Emits the @StringRes ID of the title of the [Editorial]. */
        fun title(): Observable<Int>
    }

    class EditorialViewModel(val environment: Environment, private val intent: Intent) :
        ViewModel(), Inputs, Outputs {
        private val retryContainerClicked: PublishSubject<Unit> = PublishSubject.create()

        private val description: BehaviorSubject<Int> = BehaviorSubject.create()
        private val discoveryParams: BehaviorSubject<DiscoveryParams> = BehaviorSubject.create()
        private val graphic: BehaviorSubject<Int> = BehaviorSubject.create()
        private val refreshDiscoveryFragment: PublishSubject<Unit> = PublishSubject.create()
        private val retryContainerIsGone: BehaviorSubject<Boolean> = BehaviorSubject.create()
        private val rootCategories: BehaviorSubject<List<Category>> = BehaviorSubject.create()
        private val title: BehaviorSubject<Int> = BehaviorSubject.create()

        private val apolloClient = requireNotNull(environment.apolloClientV2())

        private fun intent() = intent.let { Observable.just(it) }

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val disposables = CompositeDisposable()

        init {
            val editorial = intent()
                .map { it.getSerializableExtra(IntentKey.EDITORIAL) }
                .filter { it.isNotNull() }
                .ofType(Editorial::class.java)

            val categoriesNotification = Observable.merge(
                fetchCategories(),
                this.retryContainerClicked.switchMap { fetchCategories() }
            )

            categoriesNotification
                .compose(valuesV2())
                .map { it.filter { category -> category.isRoot } }
                .map { it.sorted() }
                .subscribe { this.rootCategories.onNext(it) }
                .addToDisposable(disposables)

            categoriesNotification
                .compose(errorsV2())
                .subscribe { this.retryContainerIsGone.onNext(false) }
                .addToDisposable(disposables)

            editorial
                .map { it.tagId }
                .map {
                    DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).tagId(it).build()
                }
                .subscribe { this.discoveryParams.onNext(it) }
                .addToDisposable(disposables)

            editorial
                .map { it.graphic }
                .subscribe { this.graphic.onNext(it) }
                .addToDisposable(disposables)

            editorial
                .map { it.title }
                .subscribe { this.title.onNext(it) }
                .addToDisposable(disposables)

            editorial
                .map { it.description }
                .subscribe { this.description.onNext(it) }
                .addToDisposable(disposables)

            this.retryContainerClicked
                .subscribe(this.refreshDiscoveryFragment)
        }

        private fun fetchCategories(): Observable<Notification<List<Category>>>? {
            return this.apolloClient.fetchCategories()
                .doOnSubscribe { this.retryContainerIsGone.onNext(true) }
                .materialize()
                .share()
        }

        override fun retryContainerClicked() = this.retryContainerClicked.onNext(Unit)

        override fun description(): Observable<Int> = this.description

        override fun discoveryParams(): Observable<DiscoveryParams> = this.discoveryParams

        override fun graphic(): Observable<Int> = this.graphic

        override fun refreshDiscoveryFragment(): Observable<Unit> = this.refreshDiscoveryFragment

        override fun retryContainerIsGone(): Observable<Boolean> = this.retryContainerIsGone

        override fun rootCategories(): Observable<List<Category>> = this.rootCategories

        override fun title(): Observable<Int> = this.title

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment, private val intent: Intent) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditorialViewModel(environment, intent) as T
        }
    }
}
