package com.kickstarter.ui.intents;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.IntentKey;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.RxLifecycle;

import rx.Observable;
import rx.functions.Action1;

public class ProjectIntentAction extends IntentAction {
  public ProjectIntentAction(final @NonNull Action1<Project> initializer, final @NonNull Observable<ActivityEvent> lifecycle,
    final @NonNull ApiClient client) {

    final Observable<Project> initialProject = intent
      .compose(RxLifecycle.bindActivity(lifecycle))
      .map(this::projectParcelable)
      .filter(ObjectUtils::isNotNull)
      .share();

    final Observable<String> initialProjectParam = intent
      .compose(RxLifecycle.bindActivity(lifecycle))
      .map(this::projectParam)
      .filter(ObjectUtils::isNotNull)
      .share();

    initialProject.subscribe(initializer);

    initialProject.map(Project::param).mergeWith(initialProjectParam)
      .filter(ObjectUtils::isNotNull)
      .switchMap(param -> client.fetchProject(param).compose(Transformers.neverError()))
      .subscribe(initializer);

  }

  private @Nullable Project projectParcelable(final @NonNull Intent intent) {
    return intent.getParcelableExtra(IntentKey.PROJECT);
  }

  private @Nullable String projectParam(final @NonNull Intent intent) {
    return intent.getStringExtra(IntentKey.PROJECT_PARAM);
  }
}
