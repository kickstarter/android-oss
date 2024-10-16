package com.kickstarter.ui.intentmappers

import android.content.Intent
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Category
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable

object DiscoveryIntentMapper {
    @JvmStatic
    fun params(
        intent: Intent,
        client: ApiClientTypeV2,
        apolloClient: ApolloClientTypeV2
    ): Observable<DiscoveryParams> {
        val paramsFromParcel = if (paramsFromIntent(intent).isNotNull()) {
            Observable.just(paramsFromIntent(intent))
                .filter { it.isNotNull() }
                .map { it }
        } else Observable.empty()

        val paramsFromUri = if (IntentMapper.uri(intent).isNotNull()) {
            Observable.just(IntentMapper.uri(intent))
                .filter { it.isNotNull() }
                .map { it }
                .map { DiscoveryParams.fromUri(it) }
                .filter { it.isNotNull() }
                .map { it }
                .flatMap { paramsFromUri(it, client, apolloClient) }
                .filter { it.isNotNull() }
                .map { it }
        } else Observable.empty()

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
        client: ApiClientTypeV2,
        apolloClient: ApolloClientTypeV2
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
        client: ApiClientTypeV2,
        apolloClient: ApolloClientTypeV2
    ): List<Observable<DiscoveryParams.Builder>> {
        val paramBuilders: MutableList<Observable<DiscoveryParams.Builder>> = ArrayList()
        val categoryParam = params.categoryParam()
        if (categoryParam != null) {
            paramBuilders.add(
                apolloClient.fetchCategory(categoryParam)
                    .compose(Transformers.neverErrorV2())
                    .filter { it.isNotNull() }
                    .map { it }
                    .map { DiscoveryParams.builder().category(it) }
            )
        }

        val locationParam = params.locationParam()
        if (locationParam != null) {
            paramBuilders.add(
                client
                    .fetchLocation(locationParam)
                    .compose(Transformers.neverErrorV2())
                    .filter { it.isNotNull() }
                    .map { it }
                    .map { DiscoveryParams.builder().location(it) }
            )
        }
        paramBuilders.add(Observable.just(params.toBuilder()))
        return paramBuilders
    }
}
