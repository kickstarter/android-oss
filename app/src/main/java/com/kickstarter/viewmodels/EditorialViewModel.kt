package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.values
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Category
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.EditorialActivity
import com.kickstarter.ui.data.Editorial
import rx.Observable
import rx.subjects.BehaviorSubject

interface EditorialViewModel {
    interface Inputs
    interface Outputs {
        /** Emits the @StringRes ID of the description of the [Editorial]. */
        fun description(): Observable<Int>

        /** Emits the [DiscoveryParams] for the tag of the [Editorial]. */
        fun discoveryParams(): Observable<DiscoveryParams>

        /** Emits the @DrawableRes of the graphic of the [Editorial]. */
        fun graphic(): Observable<Int>

        /** Emits a list of root [Category]s. */
        fun rootCategories(): Observable<List<Category>>

        /** Emits the @StringRes ID of the title of the [Editorial]. */
        fun title(): Observable<Int>
    }

    class ViewModel(val environment: Environment) : ActivityViewModel<EditorialActivity>(environment), Inputs, Outputs {
        private val description: BehaviorSubject<Int> = BehaviorSubject.create()
        private val discoveryParams: BehaviorSubject<DiscoveryParams> = BehaviorSubject.create()
        private val graphic: BehaviorSubject<Int> = BehaviorSubject.create()
        private val rootCategories: BehaviorSubject<List<Category>> = BehaviorSubject.create()
        private val title: BehaviorSubject<Int> = BehaviorSubject.create()

        private val apiClient: ApiClientType = environment.apiClient()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            val editorial = intent()
                    .map { it.getSerializableExtra(IntentKey.EDITORIAL) }
                    .filter { ObjectUtils.isNotNull(it) }
                    .ofType(Editorial::class.java)

            val categoriesNotification = this.apiClient.fetchCategories()
                    .materialize()
                    .share()

            categoriesNotification
                    .compose(values())
                    .map { it.filter { category -> category.isRoot } }
                    .map { it.sorted() }
                    .compose(bindToLifecycle())
                    .subscribe { this.rootCategories.onNext(it) }

            editorial
                    .map { it.tagId }
                    .map { DiscoveryParams.builder().sort(DiscoveryParams.Sort.HOME).tagId(it).build() }
                    .compose(bindToLifecycle())
                    .subscribe(this.discoveryParams)

            editorial
                    .map { it.graphic }
                    .compose(bindToLifecycle())
                    .subscribe(this.graphic)

            editorial
                    .map { it.title }
                    .compose(bindToLifecycle())
                    .subscribe(this.title)

            editorial
                    .map { it.description }
                    .compose(bindToLifecycle())
                    .subscribe(this.description)
        }

        override fun description(): Observable<Int> = this.description

        override fun discoveryParams(): Observable<DiscoveryParams> = this.discoveryParams

        override fun graphic(): Observable<Int> = this.graphic

        override fun rootCategories(): Observable<List<Category>> = this.rootCategories

        override fun title(): Observable<Int> = this.title
    }
}
