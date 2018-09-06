package com.kickstarter.ui.intentmappers;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;
import com.kickstarter.ui.IntentKey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;

public final class ProjectIntentMapper {
  public static final String SCHEME_KSR = "ksr";
  public static final String SCHEME_HTTPS = "https";
  private ProjectIntentMapper() {}

  // /projects/param-1/param-2*
  final static Pattern PROJECT_PATTERN = Pattern.compile("\\A\\/projects\\/([a-zA-Z0-9_-]+)(\\/([a-zA-Z0-9_-]+)).*");

  /**
   * Returns an observable of projects retrieved from intent data. May hit the API if the intent only contains a project
   * param rather than a parceled project.
   */
  public static @NonNull Observable<Project> project(final @NonNull Intent intent, final @NonNull ApiClientType client) {

    final Project intentProject = projectFromIntent(intent);
    final Observable<Project> projectFromParceledProject = intentProject == null ? Observable.empty() : Observable.just(intentProject)
      .flatMap(client::fetchProject)
      .startWith(intentProject)
      .retry(3);

    final Observable<Project> projectFromParceledParam = Observable.just(paramFromIntent(intent))
      .filter(ObjectUtils::isNotNull)
      .flatMap(client::fetchProject)
      .retry(3);

    return projectFromParceledProject
      .mergeWith(projectFromParceledParam);
  }

  /**
   * Returns a {@link RefTag} observable. If there is no parceled RefTag, emit `null`.
   */
  public static @NonNull Observable<RefTag> refTag(final @NonNull Intent intent) {
    return Observable.just(intent.getParcelableExtra(IntentKey.REF_TAG));
  }

  /**
   * Returns an observable of push notification envelopes from the intent data. This will emit only when the project
   * is launched from a push notification.
   */
  public static @NonNull Observable<PushNotificationEnvelope> pushNotificationEnvelope(final @NonNull Intent intent) {
    return Observable.just(intent.getParcelableExtra(IntentKey.PUSH_NOTIFICATION_ENVELOPE))
      .ofType(PushNotificationEnvelope.class);
  }

  /**
   * Gets a parceled project from the intent data, may return `null`.
   */
  private static @Nullable Project projectFromIntent(final @NonNull Intent intent) {
    return intent.getParcelableExtra(IntentKey.PROJECT);
  }

  /**
   * Gets a project param from the intent data, may return `null`.
   */
  private static @Nullable String paramFromIntent(final @NonNull Intent intent) {
    if (intent.hasExtra(IntentKey.PROJECT_PARAM)) {
      return intent.getStringExtra(IntentKey.PROJECT_PARAM);
    }

    return paramFromUri(IntentMapper.uri(intent));
  }


  /**
   * Extract the project param from a uri. e.g.: A uri like `ksr://www.kickstarter.com/projects/1186238668/skull-graphic-tee`
   * returns `skull-graphic-tee`.
   */
  private static @Nullable String paramFromUri(final @Nullable Uri uri) {
    if (uri == null) {
      return null;
    }

    final String scheme = uri.getScheme();
    if (!(scheme.equals(SCHEME_KSR) || scheme.equals(SCHEME_HTTPS))) {
      return null;
    }

    final Matcher matcher = PROJECT_PATTERN.matcher(uri.getPath());
    if (matcher.matches() && matcher.group(3) != null) {
      return matcher.group(3);
    }

    return null;
  }
}
