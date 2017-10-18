package com.kickstarter.viewmodels;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.services.KSUri;
import com.kickstarter.ui.activities.DeepLinkActivity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public interface DeepLinkViewModel {

  interface Inputs {
    /** Call when user clicks link that can't be deep linked. */
    void packageManager(PackageManager packageManager);
  }

  interface Outputs {
    /** Emits when we should start an external browser because we don't want to deep link. */
    Observable<String> startBrowser();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.DiscoveryActivity}. */
    Observable<Void> startDiscoveryActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}. */
    Observable<String> startProjectActivity();
  }

  final class ViewModel extends ActivityViewModel<DeepLinkActivity> implements Outputs {
    public ViewModel(@NonNull Environment environment) {
      super(environment);

      final Observable<Uri> kickstarterComUri = intent()
        .map(Intent::getData)
        .filter(ObjectUtils::isNotNull)
        .ofType(Uri.class);

      final Observable<Uri> emailUri = intent()
        .observeOn(Schedulers.io())
        .map(Intent::getData)
        .filter(ObjectUtils::isNotNull)
        .filter(uri -> uri.getHost().equals("emails.kickstarter.com"))
        .map(uri -> {
          try {
            URL originalUrl = new URL(uri.toString());
            HttpURLConnection ucon = (HttpURLConnection) originalUrl.openConnection();
            ucon.connect();
            ucon.getInputStream();
            URL redirectUrl = ucon.getURL();
            return Uri.parse(redirectUrl.toString());
          } catch (Exception e) {
            Log.e(DeepLinkViewModel.class.toString(), e.getLocalizedMessage());
            throw new RuntimeException(e);
          }
        })
        .compose(Transformers.neverError());

      Observable<Uri> uriFromIntent = Observable.merge(kickstarterComUri, emailUri)
        .filter(ObjectUtils::isNotNull);

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


      Observable<String> nonDeepLink = uriFromIntent
        .filter(uri -> !uri.getLastPathSegment().equals("projects") && !KSUri.isProjectUri(uri, uri.toString()))
        .map(Uri::toString)
        .compose(bindToLifecycle());
      nonDeepLink
        .subscribe(this.startBrowser::onNext);

      Observable<List<Intent>> targetIntents =

    }

    private final BehaviorSubject<String> startBrowser = BehaviorSubject.create();
    private final BehaviorSubject<Void> startDiscoveryActivity = BehaviorSubject.create();
    private final BehaviorSubject<String> startProjectActivity = BehaviorSubject.create();

    public final Outputs outputs = this;

    @Override
    public Observable<String> startBrowser() {
      return startBrowser;
    }
    @Override
    public Observable<Void> startDiscoveryActivity() {
      return startDiscoveryActivity;
    }
    @Override
    public Observable<String> startProjectActivity() {
      return startProjectActivity;
    }
  }
}
