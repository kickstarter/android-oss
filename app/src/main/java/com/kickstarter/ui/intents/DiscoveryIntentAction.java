package com.kickstarter.ui.intents;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.IntentKey;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.RxLifecycle;

import rx.Observable;
import rx.functions.Action1;

public final class DiscoveryIntentAction extends IntentAction {

  public DiscoveryIntentAction(final @NonNull Action1<DiscoveryParams> initializer, final @NonNull Observable<ActivityEvent> lifecycle) {
    intent
      .compose(RxLifecycle.bindActivity(lifecycle))
      .map(this::uri)
      .filter(ObjectUtils::isNotNull)
      .map(DiscoveryParams::fromUri)
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
}
