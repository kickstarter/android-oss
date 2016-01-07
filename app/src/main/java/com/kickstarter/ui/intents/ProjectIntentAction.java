package com.kickstarter.ui.intents;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.KSUri;
import com.kickstarter.ui.IntentKey;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.RxLifecycle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.functions.Action1;

public class ProjectIntentAction extends IntentAction {
  final static Pattern PROJECT_PATTERN = Pattern.compile("\\A\\/projects\\/([a-zA-Z0-9_-]+)?(\\/([a-zA-Z0-9_-]+))?");

  public ProjectIntentAction(final @NonNull Action1<Project> initializer, final @NonNull Observable<ActivityEvent> lifecycle,
    final @NonNull ApiClientType client) {

    final Observable<Project> projectFromExtra = intent
      .compose(RxLifecycle.bindActivity(lifecycle))
      .map(this::parceledProject)
      .filter(ObjectUtils::isNotNull)
      .share();

    final Observable<String> paramFromExtra = intent
      .compose(RxLifecycle.bindActivity(lifecycle))
      .map(this::extraParam)
      .filter(ObjectUtils::isNotNull)
      .share();

    final Observable<String> paramFromUri = intent
      .compose(RxLifecycle.bindActivity(lifecycle))
      .filter(ObjectUtils::isNotNull)
      .map(this::param)
      .filter(ObjectUtils::isNotNull)
      .share();

    projectFromExtra.subscribe(initializer);

    // If the intent contained a project param, fetch a project. If it
    // contained a parceled project, refresh the project to grab the latest data.
    projectFromExtra
      .map(Project::param)
      .mergeWith(paramFromExtra)
      .mergeWith(paramFromUri)
      .filter(ObjectUtils::isNotNull)
      .switchMap(param -> client.fetchProject(param).compose(Transformers.neverError()))
      .subscribe(initializer);
  }

  private @Nullable Project parceledProject(final @NonNull Intent intent) {
    return intent.getParcelableExtra(IntentKey.PROJECT);
  }

  private @Nullable String extraParam(final @NonNull Intent intent) {
    return intent.getStringExtra(IntentKey.PROJECT_PARAM);
  }

  /**
   * Attempts to retrieve project from intent. Supports both:
   *
   * /projects/creator-param/project-param
   * /projects/project-param
   */
  private @Nullable String param(final @NonNull Intent intent) {
    final Uri uri = uri(intent);
    if (uri == null) {
      return null;
    }

    final Matcher matcher = PROJECT_PATTERN.matcher(uri.getPath());
    if (matcher.matches()) {
      return matcher.group(2) != null ? matcher.group(2) : matcher.group(1);
    }

    return null;
  }
}
