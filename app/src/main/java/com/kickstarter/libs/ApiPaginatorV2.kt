package com.kickstarter.libs

import android.util.Pair
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ListUtils
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject
import java.net.MalformedURLException
import java.net.URL

/**
 * An object to facilitate loading pages of data from the API.
 *
 * @param <Data> The type of data returned from the array, e.g. `Project`, `Activity`, etc.
 * @param <Envelope> The type of envelope the API returns for a list of data, e.g. `DiscoverEnvelope`.
 * @param <Params> The type of params that [ApiClientType] can use to make a request. Many times this can just be `Void`.
</Params></Envelope></Data> */
class ApiPaginatorV2<Data, Envelope, Params> private constructor(
    private val nextPage: Observable<Unit>,
    private val startOverWith: Observable<Params>?,
    private val envelopeToListOfData: Function<Envelope, List<Data>>,
    private val loadWithParams: Function<Params, Observable<Envelope>>,
    private val loadWithPaginationPath: Function<String, Observable<Envelope>>,
    private val envelopeToMoreUrl: Function<Envelope, String>,
    private val pageTransformation: Function<List<Data>, List<Data>>?,
    private val clearWhenStartingOver: Boolean,
    private val concater: BiFunction<List<Data>, List<Data>, List<Data>>,
    private val distinctUntilChanged: Boolean
) {
    private val _morePath = PublishSubject.create<String>()
    private val _isFetching = PublishSubject.create<Boolean>()
    private lateinit var loadingPage: Observable<Int>
    private lateinit var paginatedData: Observable<List<Data>>

    // Outputs
    fun paginatedData(): Observable<List<Data>> {
        return paginatedData
    }
    val isFetching: Observable<Boolean> = _isFetching
    fun loadingPage(): Observable<Int> {
        return loadingPage
    }

    init {
        startOverWith?.let {
            paginatedData = it.switchMap { firstPageParams: Params ->
                dataWithPagination(firstPageParams)
            }
            loadingPage = it.switchMap {
                nextPage.scan(
                    1,
                    BiFunction { accum: Int, _ -> accum + 1 }
                )
            }
        }
    }

    class Builder<Data, Envelope, Params> {
        private lateinit var nextPage: Observable<Unit>
        private var startOverWith: Observable<Params>? = null
        private lateinit var envelopeToListOfData: Function<Envelope, List<Data>>
        private lateinit var loadWithParams: Function<Params, Observable<Envelope>>
        private lateinit var loadWithPaginationPath: Function<String, Observable<Envelope>>
        private lateinit var envelopeToMoreUrl: Function<Envelope, String>
        private var pageTransformation: Function<List<Data>, List<Data>>? = null
        private var clearWhenStartingOver = false
        private var concater =
            BiFunction { xs: List<Data>, ys: List<Data> -> ListUtils.concat(xs, ys) }
        private var distinctUntilChanged = false

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
         * [Required] A function to extract the more URL from an API response envelope.
         */
        fun envelopeToMoreUrl(envelopeToMoreUrl: Function<Envelope, String>): Builder<Data, Envelope, Params> {
            this.envelopeToMoreUrl = envelopeToMoreUrl
            return this
        }

        /**
         * [Required] A function that makes an API request with a pagination URL.
         */
        fun loadWithPaginationPath(loadWithPaginationPath: Function<String, Observable<Envelope>>): Builder<Data, Envelope, Params> {
            this.loadWithPaginationPath = loadWithPaginationPath
            return this
        }

        /**
         * [Required] A function that takes a `Params` and performs the associated network request
         * and returns an `Observable<Envelope>`
         </Envelope> */
        fun loadWithParams(loadWithParams: Function<Params, Observable<Envelope>>): Builder<Data, Envelope, Params> {
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

        @Throws(RuntimeException::class)
        fun build(): ApiPaginatorV2<Data, Envelope, Params> {
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
            if (loadWithPaginationPath == null) {
                throw RuntimeException("`loadWithPaginationPath` is required")
            }
            if (envelopeToMoreUrl == null) {
                throw RuntimeException("`envelopeToMoreUrl` is required")
            }

            // Default params for optional fields
            if (startOverWith == null) {
                startOverWith = Observable.empty()
            }
            if (pageTransformation == null) {
                pageTransformation = Function<List<Data>, List<Data>> { x: List<Data> -> x }
            }
            if (concater == null) {
                concater =
                    BiFunction { xs: List<Data>, ys: List<Data> -> ListUtils.concat(xs, ys) }
            }
            return ApiPaginatorV2(
                nextPage,
                startOverWith,
                envelopeToListOfData,
                loadWithParams,
                loadWithPaginationPath,
                envelopeToMoreUrl,
                pageTransformation,
                clearWhenStartingOver,
                concater,
                distinctUntilChanged
            )
        }
    }

    /**
     * Returns an observable that emits the accumulated list of paginated data each time a new page is loaded.
     */
    private fun dataWithPagination(firstPageParams: Params): Observable<List<Data>> {
        val data = paramsAndMoreUrlWithPagination(firstPageParams)
            .concatMap { paginatingData: Pair<Params, String> -> fetchData(paginatingData) }
            .takeUntil { it.isEmpty() }

        val paginatedData =
            if (clearWhenStartingOver) data.scan(ArrayList(), concater) else data.scan(
                concater
            )
        return if (distinctUntilChanged) paginatedData.distinctUntilChanged() else paginatedData
    }

    /**
     * Returns an observable that emits the params for the next page of data *or* the more URL for the next page.
     */
    private fun paramsAndMoreUrlWithPagination(firstPageParams: Params): Observable<Pair<Params, String>> {
        return _morePath
            .map<Pair<Params, String>>(Function { path: String -> Pair(null, path) })
            .compose<Pair<Params, String>>(Transformers.takeWhenV2(nextPage))
            .startWith(Pair(firstPageParams, null))
    }

    @Throws(Exception::class)
    private fun fetchData(paginatingData: Pair<Params, String>): Observable<List<Data>> {
        return (
            if (paginatingData.second != null) loadWithPaginationPath.apply(paginatingData.second!!) else loadWithParams.apply(
                paginatingData.first
            )
            )
            .retry(2)
            .compose(Transformers.neverErrorV2())
            .doOnNext { envelope: Envelope -> keepMorePath(envelope) }
            .map(envelopeToListOfData)
            .map(pageTransformation)
            .takeUntil { it.isEmpty() }
            .doOnSubscribe { _isFetching.onNext(true) }
            .doAfterTerminate { _isFetching.onNext(false) }
            .doFinally { _isFetching.onNext(false) }
    }

    private fun keepMorePath(envelope: Envelope) {
        try {
            val url = URL(envelopeToMoreUrl.apply(envelope))
            _morePath.onNext(pathAndQueryFromURL(url))
        } catch (ignored: MalformedURLException) {
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun pathAndQueryFromURL(url: URL): String {
        return url.path + "?" + url.query
    }

    companion object {
        fun <Data, Envelope, FirstPageParams> builder(): Builder<Data, Envelope, FirstPageParams> {
            return Builder()
        }
    }
}
