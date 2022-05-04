package com.kickstarter.ui.intentmappers

import android.content.Intent
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Category
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.IntentKey
import rx.Observable

object DiscoveryIntentMapper {
    @JvmStatic
    fun params(
        intent: Intent,
        client: ApiClientType,
        apolloClient: ApolloClientType
    ): Observable<DiscoveryParams> {
        val paramsFromParcel = Observable.just(paramsFromIntent(intent))
            .filter {
                ObjectUtils.isNotNull(it)
            }.map { requireNotNull(it) }

        val paramsFromUri = Observable.just(IntentMapper.uri(intent))
            .filter { ObjectUtils.isNotNull(it) }
            .map { requireNotNull(it) }
            .map { DiscoveryParams.fromUri(it) }
            .flatMap {
                paramsFromUri(it, client, apolloClient)
            }
        return Observable.merge(paramsFromParcel, paramsFromUri)
    }

    private fun paramsFromIntent(intent: Intent): DiscoveryParams? {
        return intent.getParcelableExtra(IntentKey.DISCOVERY_PARAMS)
    }

    /**
     * Returns params where category and location params have been converted into [Category]
     * and [Location] objects.
     */
    private fun paramsFromUri(
        params: DiscoveryParams,
        client: ApiClientType,
        apolloClient: ApolloClientType
    ): Observable<DiscoveryParams> {
        return Observable.zip(paramBuilders(params, client, apolloClient)) {
            var builder = DiscoveryParams.builder()
            for (item in it) {
                (item as? DiscoveryParams.Builder)?.let { b ->
                    builder = builder.mergeWith(b)
                }
            }
            builder.build()
        }
    }

    /**
     * Creates observables that will perform API requests to retrieve additional data needed to fill out
     * a full discovery params object. For example, if `params` holds only a category slug and no actual
     * category data, we will perform a request to get the full category from the API.
     * @param params The discovery params that is potentially missing full data.
     * @return A list of observables, each responsible for retrieving more data from the API. The
     * observables emit *builders* of params, and hence can later be merged into a single params object.
     */
    private fun paramBuilders(
        params: DiscoveryParams,
        client: ApiClientType,
        apolloClient: ApolloClientType
    ): List<Observable<DiscoveryParams.Builder>> {
        val paramBuilders: MutableList<Observable<DiscoveryParams.Builder>> = ArrayList()
        val categoryParam = params.categoryParam()
        if (categoryParam != null) {
            paramBuilders.add(
                apolloClient.fetchCategory(categoryParam)
                    .compose(Transformers.neverError())
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }
                    .map { DiscoveryParams.builder().category(it) }
            )
        }

        val locationParam = params.locationParam()
        if (locationParam != null) {
            paramBuilders.add(
                client
                    .fetchLocation(locationParam)
                    .compose(Transformers.neverError())
                    .map { DiscoveryParams.builder().location(it) }
            )
        }
        paramBuilders.add(Observable.just(params.toBuilder()))
        return paramBuilders
    }
}
