package com.kickstarter.libs.loadmore

import android.util.Pair
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.ApolloEnvelope
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.net.MalformedURLException

class ApolloPaginateV2<Data, Envelope : ApolloEnvelope, Params>(
    val nextPage: Observable<Unit>,
    val startOverWith: Observable<Params>?,
    val envelopeToListOfData: Function<Envelope, List<Data>>,
    val loadWithParams: Function<Pair<Params, String>, Observable<Envelope>>,
    val pageTransformation: Function<List<Data>, List<Data>>?,
    val clearWhenStartingOver: Boolean = true,
    val concater: BiFunction<List<Data>, List<Data>, List<Data>>,
    val distinctUntilChanged: Boolean,
    val isReversed: Boolean
) {
    private val _morePath = PublishSubject.create<String>()
    val isFetching = BehaviorSubject.create<Boolean>()
    private lateinit var loadingPage: Observable<Int>
    private lateinit var paginatedData: Observable<List<Data>>

    val dummyObserver = BehaviorSubject.create<Boolean>()
    init {
        startOverWith?.let {
            paginatedData =
                it.switchMap { firstPageParams: Params ->
                    dataWithPagination(firstPageParams)
                }
            loadingPage =
                it.switchMap<Int> {
                    nextPage.scan(1, { accum: Int, _ -> accum + 1 })
                }
        }
    }

    class Builder<Data, Envelope : ApolloEnvelope, Params> {
        private lateinit var nextPage: Observable<Unit>
        private var startOverWith: Observable<Params>? = null
        private lateinit var envelopeToListOfData: Function<Envelope, List<Data>>
        private lateinit var loadWithParams: Function<Pair<Params, String>, Observable<Envelope>>
        private var pageTransformation: Function<List<Data>, List<Data>> = Function<List<Data>, List<Data>> {
                x: List<Data> ->
            x
        }
        private var clearWhenStartingOver = false

        private var concater: BiFunction<List<Data>, List<Data>, List<Data>> =
            BiFunction { xs: List<Data>, ys: List<Data> ->
                mutableListOf<Data>().apply {
                    if (isReversed) {
                        ys?.toMutableList()?.let { this.addAll(it) }
                        xs?.toMutableList()?.let { this.addAll(it) }
                    } else {
                        xs?.toMutableList()?.let { this.addAll(it) }
                        ys?.toMutableList()?.let { this.addAll(it) }
                    }
                }.toList()
            }
        private var distinctUntilChanged = false
        private var isReversed = false

        /**
         * [Required] An observable that emits whenever a new page of data should be loaded.
         */
        fun nextPage(nextPage: Observable<Unit>): Builder<Data, Envelope, Params> {
            this.nextPage = nextPage
            return this
        }

        /**
         * [Optional] An observable that emits when a fresh first page should be loaded.
         */
        fun startOverWith(startOverWith: Observable<Params>): Builder<Data, Envelope, Params> {
            this.startOverWith = startOverWith
            return this
        }

        /**
         * [Required] A function that takes an `Envelope` instance and returns the list of data embedded in it.
         */
        fun envelopeToListOfData(envelopeToListOfData: Function<Envelope, List<Data>>): Builder<Data, Envelope, Params> {
            this.envelopeToListOfData = envelopeToListOfData
            return this
        }

        /**
         * [Required] A function that takes a `Params` and performs the associated network request
         * and returns an `Observable<Envelope>`
         </Envelope> */
        fun loadWithParams(loadWithParams: Function<Pair<Params, String>, Observable<Envelope>>): Builder<Data, Envelope, Params> {
            this.loadWithParams = loadWithParams
            return this
        }

        /**
         * [Optional] Function to transform every page of data that is loaded.
         */
        fun pageTransformation(pageTransformation: Function<List<Data>, List<Data>>): Builder<Data, Envelope, Params> {
            this.pageTransformation = pageTransformation
            return this
        }

        /**
         * [Optional] Determines if the list of loaded data is cleared when starting over from the first page.
         */
        fun clearWhenStartingOver(clearWhenStartingOver: Boolean): Builder<Data, Envelope, Params> {
            this.clearWhenStartingOver = clearWhenStartingOver
            return this
        }

        /**
         * [Optional] Determines how two lists are concatenated together while paginating. A regular `ListUtils::concat` is probably
         * sufficient, but sometimes you may want `ListUtils::concatDistinct`
         */
        fun concater(concater: BiFunction<List<Data>, List<Data>, List<Data>>): Builder<Data, Envelope, Params> {
            this.concater = concater
            return this
        }

        /**
         * [Optional] Determines if the list of loaded data is should be distinct until changed.
         */
        fun distinctUntilChanged(distinctUntilChanged: Boolean): Builder<Data, Envelope, Params> {
            this.distinctUntilChanged = distinctUntilChanged
            return this
        }

        /**
         * [Optional] Determines if the list of loaded data is should be distinct until changed.
         */
        fun isReversed(isReversed: Boolean): Builder<Data, Envelope, Params> {
            this.isReversed = isReversed
            return this
        }

        @Throws(RuntimeException::class)
        fun build(): ApolloPaginateV2<Data, Envelope, Params> {
            // Early error when required field is not set
            if (nextPage == null) {
                throw RuntimeException("`nextPage` is required")
            }
            if (envelopeToListOfData == null) {
                throw RuntimeException("`envelopeToListOfData` is required")
            }
            if (loadWithParams == null) {
                throw RuntimeException("`loadWithParams` is required")
            }

            // Default params for optional fields
            if (startOverWith == null) {
                startOverWith = Observable.just(null)
            }

            return ApolloPaginateV2(
                requireNotNull(nextPage),
                requireNotNull(startOverWith),
                requireNotNull(envelopeToListOfData),
                requireNotNull(loadWithParams),
                pageTransformation,
                clearWhenStartingOver,
                concater,
                distinctUntilChanged,
                isReversed
            )
        }
    }

    companion object {
        @JvmStatic
        fun <Data, Envelope : ApolloEnvelope, FirstPageParams> builder(): Builder<Data, Envelope, FirstPageParams> = Builder()
    }

    /**
     * Returns an observable that emits the accumulated list of paginated data each time a new page is loaded.
     */
    private fun dataWithPagination(firstPageParams: Params): Observable<List<Data>>? {
        val data = paramsAndMoreUrlWithPagination(firstPageParams)
            ?.concatMap { fetchData(it) }
            ?.takeUntil { it.isEmpty() }

        val paginatedData =
            if (clearWhenStartingOver)
                data?.scan(ArrayList(), concater)
            else
                data?.scan(concater)

        return if (distinctUntilChanged)
            paginatedData?.distinctUntilChanged()
        else
            paginatedData
    }

    /**
     * Returns an observable that emits the params for the next page of data *or* the more URL for the next page.
     */
    private fun paramsAndMoreUrlWithPagination(firstPageParams: Params): Observable<Pair<Params, String>>? {
        return _morePath
            .map { path: String ->
                Pair<Params, String>(
                    firstPageParams,
                    path
                )
            }
            .compose(Transformers.takeWhenV2(nextPage))
            .startWith(Pair(firstPageParams, ""))
    }

    private fun fetchData(paginatingData: Pair<Params, String>): Observable<List<Data>> {

        return loadWithParams.apply(paginatingData)
            .retry(2)
            .compose(Transformers.neverErrorV2())
            .doOnNext { envelope: Envelope ->
                keepMorePath(envelope)
            }
            .map(envelopeToListOfData)
            .map(this.pageTransformation)
            .takeUntil { data: List<Data> -> data.isEmpty() }
            .doOnSubscribe {
                isFetching.onNext(true)
            }
            .doAfterTerminate {
                isFetching.onNext(false)
            }
            .doFinally {
                isFetching.onNext(false)
            }
    }

    private fun keepMorePath(envelope: Envelope) {
        try {
            (
                if (isReversed)
                    envelope.pageInfoEnvelope()?.startCursor
                else
                    envelope.pageInfoEnvelope()?.endCursor
                )?.let {
                _morePath.onNext(
                    it
                )
            }
        } catch (ignored: MalformedURLException) {
            ignored.printStackTrace()
        }
    }
    fun paginatedData(): Observable<List<Data>>? {
        return paginatedData
    }

    fun loadingPage(): Observable<Int> {
        return loadingPage
    }
}
