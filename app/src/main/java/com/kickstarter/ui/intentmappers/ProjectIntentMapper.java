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
import com.kickstarter.ui.IntentKey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;

public final class ProjectIntentMapper {
  private ProjectIntentMapper() {}

  final static Pattern PROJECT_PATTERN = Pattern.compile("\\A\\/projects\\/([a-zA-Z0-9_-]+)(\\/([a-zA-Z0-9_-]+)).*");

  public static Observable<Project> project(final @NonNull Intent intent, final @NonNull ApiClientType client) {

    final Observable<Project> parceledProjectFromIntent = Observable.just(projectFromIntent(intent))
      .filter(ObjectUtils::isNotNull);

    final Observable<String> paramFromParceledProject = parceledProjectFromIntent.map(Project::param);

    final Observable<String> paramFromIntent = Observable.just(paramFromIntent(intent))
      .filter(ObjectUtils::isNotNull);

    return paramFromParceledProject
      .mergeWith(paramFromIntent)
      .flatMap(client::fetchProject)
      .retry(3)
      .compose(Transformers.neverError())
      .startWith(parceledProjectFromIntent);
  }

  /**
   * Returns a {@link RefTag} observable. If there is no parceled RefTag, emit `null`.
   */
  public static @NonNull Observable<RefTag> refTag(final @NonNull Intent intent) {
    return Observable.just(intent.getParcelableExtra(IntentKey.REF_TAG));
  }

  private static @Nullable Project projectFromIntent(final @NonNull Intent intent) {
    return intent.getParcelableExtra(IntentKey.PROJECT);
  }

  private static @Nullable String paramFromIntent(final @NonNull Intent intent) {
    if (intent.hasExtra(IntentKey.PROJECT_PARAM)) {
      return intent.getStringExtra(IntentKey.PROJECT_PARAM);
    }

    return paramFromUri(IntentMapper.uri(intent));
  }

  private static @Nullable String paramFromUri(final @Nullable Uri uri) {
    if (uri == null) {
      return null;
    }

    if (!uri.getScheme().equals("ksr")) {
      return null;
    }

    final Matcher matcher = PROJECT_PATTERN.matcher(uri.getPath());
    if (matcher.matches() && matcher.group(3) != null) {
      return matcher.group(3);
    }

    return null;
  }
}
