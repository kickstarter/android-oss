package com.kickstarter.ui.intents;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.IntentKey;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.RxLifecycle;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;

public final class DiscoveryIntentAction extends IntentAction {
  private final ApiClientType client;

  public DiscoveryIntentAction(final @NonNull Action1<DiscoveryParams> initializer,
    final @NonNull Observable<ActivityEvent> lifecycle, final @NonNull ApiClientType client) {
    this.client = client;

    intent
      .compose(RxLifecycle.bindActivity(lifecycle))
      .map(this::uri)
      .filter(ObjectUtils::isNotNull)
      .map(DiscoveryParams::fromUri)
      .flatMap(this::fromUri)
      .subscribe(initializer);

    intent
      .compose(RxLifecycle.bindActivity(lifecycle))
      .map(this::parceledParams)
      .filter(ObjectUtils::isNotNull)
      .subscribe(initializer);
  }

  private @Nullable DiscoveryParams parceledParams(final @NonNull Intent intent) {
    return intent.getParcelableExtra(IntentKey.DISCOVERY_PARAMS);
  }

  /**
   * Returns params where category and location params have been converted into {@link Category}
   * and {@link Location} objects.
   */
  private @NonNull Observable<DiscoveryParams> fromUri(final @NonNull DiscoveryParams params) {
    return Observable.zip(paramBuilders(params), builders -> {
      DiscoveryParams.Builder builder = DiscoveryParams.builder();

      for (final Object object : builders) {
        final DiscoveryParams.Builder b = (DiscoveryParams.Builder) object;
        builder = builder.mergeWith(b);
      }

      return builder.build();
    });
  }

  private @NonNull List<Observable<DiscoveryParams.Builder>> paramBuilders(final @NonNull DiscoveryParams params) {
    final List<Observable<DiscoveryParams.Builder>> paramBuilders = new ArrayList<>();

    paramBuilders.add(Observable.just(params.toBuilder()));

    final String categoryParam = params.categoryParam();
    if (categoryParam != null) {
      paramBuilders.add(
        client
          .fetchCategory(categoryParam)
          .map(c -> DiscoveryParams.builder().category(c))
      );
    }

    final String locationParam = params.locationParam();
    if (locationParam != null) {
      paramBuilders.add(
        client
          .fetchLocation(locationParam)
          .map(l -> DiscoveryParams.builder().location(l))
      );
    }

    return paramBuilders;
  }
}
