package com.kickstarter.viewmodels;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.services.KSUri;
import com.kickstarter.ui.activities.DeepLinkActivity;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

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
    Observable<String> requestPackageManager();

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
    Observable<String> startProjectActivity();
  }

  final class ViewModel extends ActivityViewModel<DeepLinkActivity> implements Outputs, Inputs {
    public ViewModel(@NonNull final Environment environment) {
      super(environment);

      final Observable<Uri> uriFromIntent = intent()
        .map(Intent::getData)
        .filter(ObjectUtils::isNotNull)
        .ofType(Uri.class);

      uriFromIntent
        .filter(uri -> uri.getLastPathSegment().equals("projects"))
        .compose(Transformers.ignoreValues())
        .compose(bindToLifecycle())
        .subscribe(this.startDiscoveryActivity::onNext);

      this.startDiscoveryActivity
        .subscribe(__ -> koala.trackUserActivity());

      uriFromIntent
        .filter(uri -> KSUri.isProjectUri(uri, uri.toString()))
        .map(Uri::toString)
        .compose(bindToLifecycle())
        .subscribe(this.startProjectActivity::onNext);

      this.startProjectActivity
        .subscribe(__ -> koala.trackUserActivity());


      final Observable<Pair<PackageManager, Uri>> packageManagerAndUri =
        Observable.combineLatest(this.packageManager, uriFromIntent, Pair::create);

      final Observable<List<Intent>> targetIntents = packageManagerAndUri
        .flatMap(pair -> {
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

      uriFromIntent
        .filter(uri -> !uri.getLastPathSegment().equals("projects") && !KSUri.isProjectUri(uri, uri.toString()))
        .map(Uri::toString)
        .compose(bindToLifecycle())
        .subscribe(this.requestPackageManager::onNext);

    }
    private final PublishSubject<PackageManager> packageManager = PublishSubject.create();

    private final BehaviorSubject<String> requestPackageManager = BehaviorSubject.create();
    private final BehaviorSubject<List<Intent>> startBrowser = BehaviorSubject.create();
    private final BehaviorSubject<Void> startDiscoveryActivity = BehaviorSubject.create();
    private final BehaviorSubject<String> startProjectActivity = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override
    public void packageManager(final PackageManager packageManager) {
      this.packageManager.onNext(packageManager);
    }

    @Override
    public Observable<String> requestPackageManager() {
      return this.requestPackageManager;
    }
    @Override
    public Observable<List<Intent>> startBrowser() {
      return this.startBrowser;
    }
    @Override
    public Observable<Void> startDiscoveryActivity() {
      return this.startDiscoveryActivity;
    }
    @Override
    public Observable<String> startProjectActivity() {
      return this.startProjectActivity;
    }
  }
}
