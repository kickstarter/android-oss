package com.kickstarter.ui.intentmappers;

import android.content.Intent;

import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.IntentKey;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.Observable;

public final class DiscoveryIntentMapper {
  private DiscoveryIntentMapper() {}

  public static Observable<DiscoveryParams> params(final @NonNull Intent intent,
    final @NonNull ApiClientType client) {

    final Observable<DiscoveryParams> paramsFromParcel = Observable.just(paramsFromIntent(intent))
      .filter(ObjectUtils::isNotNull);

    final Observable<DiscoveryParams> paramsFromUri = Observable.just(IntentMapper.uri(intent))
      .filter(ObjectUtils::isNotNull)
      .map(DiscoveryParams::fromUri)
      .flatMap(uri -> paramsFromUri(uri, client));

    return Observable.merge(paramsFromParcel, paramsFromUri);
  }

  private static @Nullable DiscoveryParams paramsFromIntent(final @NonNull Intent intent) {
    return intent.getParcelableExtra(IntentKey.DISCOVERY_PARAMS);
  }

  /**
   * Returns params where category and location params have been converted into {@link Category}
   * and {@link Location} objects.
   */
  private static @NonNull Observable<DiscoveryParams> paramsFromUri(final @NonNull DiscoveryParams params,
    final @NonNull ApiClientType client) {
    return Observable.zip(paramBuilders(params, client), builders -> {
      DiscoveryParams.Builder builder = DiscoveryParams.builder();

      for (final Object object : builders) {
        final DiscoveryParams.Builder b = (DiscoveryParams.Builder) object;
        builder = builder.mergeWith(b);
      }

      return builder.build();
    });
  }

  /**
   * Creates observables that will perform API requests to retrieve additional data needed to fill out
   * a full discovery params object. For example, if `params` holds only a category slug and no actual
   * category data, we will perform a request to get the full category from the API.
   * @param params The discovery params that is potentially missing full data.
   * @return A list of observables, each responsible for retrieving more data from the API. The
   * observables emit *builders* of params, and hence can later be merged into a single params object.
   */
  private static @NonNull List<Observable<DiscoveryParams.Builder>> paramBuilders(final @NonNull DiscoveryParams params,
    final @NonNull ApiClientType client) {

    final List<Observable<DiscoveryParams.Builder>> paramBuilders = new ArrayList<>();

    final String categoryParam = params.categoryParam();
    if (categoryParam != null) {
      paramBuilders.add(
        client
          .fetchCategory(categoryParam)
          .map(c -> DiscoveryParams.builder().category(c))
          .compose(Transformers.neverError())
      );
    }

    final String locationParam = params.locationParam();
    if (locationParam != null) {
      paramBuilders.add(
        client
          .fetchLocation(locationParam)
          .map(l -> DiscoveryParams.builder().location(l))
          .compose(Transformers.neverError())
      );
    }

    paramBuilders.add(Observable.just(params.toBuilder()));

    return paramBuilders;
  }
}
