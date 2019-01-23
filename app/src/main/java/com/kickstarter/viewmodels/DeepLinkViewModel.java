package com.kickstarter.viewmodels;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.services.KSUri;
import com.kickstarter.ui.activities.DeepLinkActivity;

import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;

public interface DeepLinkViewModel {

  interface Inputs {
    /**
     * Call when user clicks link that can't be deep linked.
     */
    void packageManager(PackageManager packageManager);
  }

  interface Outputs {
    /**
     * Emits when we need to get {@link PackageManager} to query for activities that can open a link.
     */
    Observable<Void> requestPackageManager();

    /**
     * Emits when we should start an external browser because we don't want to deep link.
     */
    Observable<List<Intent>> startBrowser();

    /**
     * Emits when we should start the {@link com.kickstarter.ui.activities.DiscoveryActivity}.
     */
    Observable<Void> startDiscoveryActivity();

    /**
     * Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}.
     */
    Observable<Uri> startProjectActivity();
  }

  final class ViewModel extends ActivityViewModel<DeepLinkActivity> implements Outputs, Inputs {
    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<Uri> uriFromIntent = intent()
        .map(Intent::getData)
        .ofType(Uri.class);

      uriFromIntent
        .filter(this::lastPathSegmentIsProjects)
        .compose(ignoreValues())
        .compose(bindToLifecycle())
        .subscribe(this.startDiscoveryActivity::onNext);

      this.startDiscoveryActivity
        .subscribe(__ -> koala.trackContinueUserActivityAndOpenedDeepLink());

      uriFromIntent
        .filter(uri -> KSUri.isProjectUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .filter(uri -> !KSUri.isProjectPreviewUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .compose(bindToLifecycle())
        .subscribe(this.startProjectActivity::onNext);

      this.startProjectActivity
        .subscribe(__ -> koala.trackContinueUserActivityAndOpenedDeepLink());

      final Observable<Pair<PackageManager, Uri>> packageManagerAndUri =
        Observable.combineLatest(this.packageManager, uriFromIntent, Pair::create);

      final Observable<List<Intent>> targetIntents = packageManagerAndUri
        .flatMap(pair -> {
          /* We use a fake Uri because in Android 6.0 and above,
          if a link is domain verified, only that app is returned. */
          final Uri fakeUri = Uri.parse("http://www.kickstarter.com");
          final Intent browserIntent = new Intent(Intent.ACTION_VIEW, fakeUri);
          return Observable.from(pair.first.queryIntentActivities(browserIntent, 0))
            .filter(resolveInfo -> !resolveInfo.activityInfo.packageName.contains("com.kickstarter"))
            .map(resolveInfo -> {
              final Intent intent = new Intent(Intent.ACTION_VIEW, pair.second);
              intent.setPackage(resolveInfo.activityInfo.packageName);
              intent.setData(pair.second);
              return intent;
            })
            .toList();
        });

      targetIntents
        .compose(bindToLifecycle())
        .subscribe(this.startBrowser::onNext);

      final Observable<Uri> projectPreview = uriFromIntent
        .filter(uri -> KSUri.isProjectPreviewUri(uri, Secrets.WebEndpoint.PRODUCTION));

      final Observable<Uri> unsupportedDeepLink = uriFromIntent
        .filter(uri -> !lastPathSegmentIsProjects(uri))
        .filter(uri -> !KSUri.isProjectUri(uri, Secrets.WebEndpoint.PRODUCTION));

      Observable.merge(projectPreview, unsupportedDeepLink)
        .map(Uri::toString)
        .filter(url -> !TextUtils.isEmpty(url))
        .compose(ignoreValues())
        .compose(bindToLifecycle())
        .subscribe(this.requestPackageManager::onNext);
    }

    private boolean lastPathSegmentIsProjects(final @NonNull Uri uri) {
      return uri.getLastPathSegment().equals("projects");
    }

    private final PublishSubject<PackageManager> packageManager = PublishSubject.create();

    private final BehaviorSubject<Void> requestPackageManager = BehaviorSubject.create();
    private final BehaviorSubject<List<Intent>> startBrowser = BehaviorSubject.create();
    private final BehaviorSubject<Void> startDiscoveryActivity = BehaviorSubject.create();
    private final BehaviorSubject<Uri> startProjectActivity = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void packageManager(final PackageManager packageManager) {
      this.packageManager.onNext(packageManager);
    }

    @Override public @NonNull Observable<Void> requestPackageManager() {
      return this.requestPackageManager;
    }
    @Override public @NonNull Observable<List<Intent>> startBrowser() {
      return this.startBrowser;
    }
    @Override public @NonNull Observable<Void> startDiscoveryActivity() {
      return this.startDiscoveryActivity;
    }
    @Override public @NonNull Observable<Uri> startProjectActivity() {
      return this.startProjectActivity;
    }
  }
}
